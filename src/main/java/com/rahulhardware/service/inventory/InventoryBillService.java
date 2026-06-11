package com.rahulhardware.service.inventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahulhardware.dto.inventory.ConfirmInventoryBillRequest;
import com.rahulhardware.dto.inventory.InventoryBillPreviewResponse;
import com.rahulhardware.dto.inventory.InventoryBillProductPreview;
import com.rahulhardware.dto.inventory.InventoryBillRequest;
import com.rahulhardware.entity.Product;
import com.rahulhardware.repository.ProductRepository;

@Service
public class InventoryBillService {

    private static class ProductMatch {
        private final Product product;
        private final int score;

        private ProductMatch(Product product, int score) {
            this.product = product;
            this.score = score;
        }
    }

    private final ProductRepository productRepository;
    private final GeminiBillParserService geminiBillParserService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InventoryBillService(
            ProductRepository productRepository,
            GeminiBillParserService geminiBillParserService,
            JdbcTemplate jdbcTemplate) {

        this.productRepository = productRepository;
        this.geminiBillParserService = geminiBillParserService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public InventoryBillPreviewResponse extractProducts(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("Please upload a valid bill file.");
            }

            String originalFileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
            String fileName = originalFileName.toLowerCase();
            String fileType = getFileType(fileName);

            List<InventoryBillProductPreview> products = geminiBillParserService.extractProductsFromBill(file);

            fixMissingQuantityFromPriceAndTotal(products);

            products = matchProductsWithDatabase(products);

            InventoryBillPreviewResponse response = buildResponse(originalFileName, fileType, products);

            printPrettyJson("FINAL INVENTORY BILL API RESPONSE", response);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Unable to process inventory bill using Gemini AI: " + e.getMessage(), e);
        }
    }

    public void saveProducts(List<InventoryBillRequest> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        for (InventoryBillRequest dto : products) {
            Long productId = parseLong(dto.getProductId());
            Integer billQty = dto.getBillQuantity() == null ? 0 : dto.getBillQuantity();

            if (billQty <= 0) {
                continue;
            }

            if (productId != null) {
                Product existingProduct = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

                int currentStock = existingProduct.getStockQuantity() == null ? 0 : existingProduct.getStockQuantity();
                existingProduct.setStockQuantity(currentStock + billQty);

                if (dto.getPrice() != null && dto.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                    existingProduct.setPrice(dto.getPrice());
                }

                if (dto.getUnit() != null && !dto.getUnit().isBlank()) {
                    existingProduct.setUnit(dto.getUnit());
                }

                productRepository.save(existingProduct);
            } else {
                if (dto.getProductName() == null || dto.getProductName().isBlank()) {
                    continue;
                }

                Product product = new Product();

                product.setName(dto.getProductName().trim());
                product.setDescription("");
                product.setPrice(dto.getPrice() == null ? BigDecimal.ZERO : dto.getPrice());
                product.setStockQuantity(billQty);
                product.setUnit(dto.getUnit() == null || dto.getUnit().isBlank() ? "PCS" : dto.getUnit());
                product.setActive(true);
                product.setImageUrl(dto.getImageUrl() == null ? "" : dto.getImageUrl());
                product.setCategoryId(dto.getCategoryId());
                product.setSubCategoryId(dto.getSubCategoryId());

                productRepository.save(product);
            }
        }
    }

    @Transactional
    public void confirmAndUpdateStock(ConfirmInventoryBillRequest request) {
        if (request == null || request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new RuntimeException("No products found to update stock.");
        }

        int rowNumber = 0;

        for (InventoryBillProductPreview item : request.getProducts()) {
            rowNumber++;

            validateProductBeforeStockUpdate(item, rowNumber);

            Integer billQty = item.getBillQuantity();

            ensureCategoryAndSubCategoryExist(item);

            Product product = null;

            if (!isInvalid(item.getProductId())) {
                product = productRepository.findById(Long.valueOf(item.getProductId()))
                        .orElse(null);
            }

            if (product == null && !isInvalid(item.getProductName())) {
                product = productRepository.findFirstByNameIgnoreCase(item.getProductName().trim())
                        .orElse(null);
            }

            if (product == null) {
                product = new Product();
                product.setName(item.getProductName().trim());
                product.setDescription(item.getDescription() == null ? "" : item.getDescription());
                product.setStockQuantity(0);
                product.setLowStockLimit(10);
                product.setActive(true);
                product.setImageUrl(item.getImageUrl() == null ? "" : item.getImageUrl());
            }

            product.setCategoryId(item.getCategoryId());
            product.setSubCategoryId(item.getSubCategoryId());
            product.setPrice(item.getPrice());
            product.setUnit(isInvalid(item.getUnit()) ? "PCS" : item.getUnit().trim());

            Integer oldStock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
            product.setStockQuantity(oldStock + billQty);

            productRepository.save(product);
        }
    }

    private void validateProductBeforeStockUpdate(InventoryBillProductPreview item, int rowNumber) {
        if (item == null) {
            throw new RuntimeException("Invalid product found at item #" + rowNumber + ".");
        }

        String productName = item.getProductName();
        String category = firstValid(item.getCategoryName(), item.getCategoryId());
        String subCategory = firstValid(item.getSubCategoryName(), item.getSubCategoryId());

        if (isInvalid(productName)
                || isInvalid(category)
                || isInvalid(subCategory)
                || item.getBillQuantity() == null
                || item.getBillQuantity() <= 0
                || item.getPrice() == null
                || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Please complete product classification before updating stock. "
                            + "Product Name, Category, Sub-Category, Quantity and Price are mandatory for item #"
                            + rowNumber
                            + ".");
        }
    }

    private boolean isInvalid(String value) {
        if (value == null) {
            return true;
        }

        String text = value.trim().toLowerCase();

        return text.isEmpty()
                || text.equals("na")
                || text.equals("n/a")
                || text.equals("null")
                || text.equals("undefined")
                || text.equals("uncategorized")
                || text.startsWith("uncategorized-")
                || text.startsWith("uncategorized_");
    }

    private String firstValid(String primary, String fallback) {
        if (!isInvalid(primary)) {
            return primary.trim();
        }

        if (!isInvalid(fallback)) {
            return fallback.trim();
        }

        return "";
    }

    private void ensureCategoryAndSubCategoryExist(
        InventoryBillProductPreview item) {

    String categoryName =
            firstValid(item.getCategoryName(), item.getCategoryId());

    String subCategoryName =
            firstValid(item.getSubCategoryName(), item.getSubCategoryId());

    if (isInvalid(categoryName) || isInvalid(subCategoryName)) {
        throw new RuntimeException(
                "Category and Sub-Category are mandatory. Please review product: "
                        + (item.getProductName() == null
                        ? "Unnamed Product"
                        : item.getProductName()));
    }

    // -----------------------------------
    // CATEGORY
    // -----------------------------------

    String categoryId = jdbcTemplate.query(
            "SELECT id FROM categories WHERE LOWER(name)=LOWER(?) LIMIT 1",
            rs -> rs.next() ? rs.getString("id") : null,
            categoryName.trim());

    if (categoryId == null) {

        categoryId =
                generateSlug(categoryName);

        jdbcTemplate.update(
                """
                INSERT INTO categories
                (id,name,image_url,display_order,active)
                VALUES (?,?,?,?,?)
                """,
                categoryId,
                categoryName.trim(),
                "",
                999,
                true);
    }

    // -----------------------------------
    // SUB CATEGORY
    // -----------------------------------

    String subCategoryId = jdbcTemplate.query(
            """
            SELECT id
            FROM sub_categories
            WHERE LOWER(name)=LOWER(?)
            AND category_id=?
            LIMIT 1
            """,
            rs -> rs.next() ? rs.getString("id") : null,
            subCategoryName.trim(),
            categoryId);

    if (subCategoryId == null) {

        subCategoryId =
                categoryId + "-"
                        + generateSlug(subCategoryName);

        jdbcTemplate.update(
                """
                INSERT INTO sub_categories
                (id,name,image_url,category_id,display_order,active)
                VALUES (?,?,?,?,?,?)
                """,
                subCategoryId,
                subCategoryName.trim(),
                "",
                categoryId,
                999,
                true);
    }

    item.setCategoryId(categoryId);
    item.setSubCategoryId(subCategoryId);

    item.setCategoryName(categoryName.trim());
    item.setSubCategoryName(subCategoryName.trim());
}

    private String generateSlug(String value) {
        if (value == null) {
            return "";
        }

        String slug = value
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");

        if (slug.length() > 80) {
            slug = slug.substring(0, 80).replaceAll("-+$", "");
        }

        return slug;
    }

    private void fixMissingQuantityFromPriceAndTotal(List<InventoryBillProductPreview> products) {
        if (products == null) {
            return;
        }

        for (InventoryBillProductPreview item : products) {
            Integer qty = item.getBillQuantity();

            if (qty != null && qty > 0) {
                continue;
            }

            BigDecimal price = item.getPrice();
            BigDecimal total = item.getTotalAmount();

            if (price != null
                    && total != null
                    && price.compareTo(BigDecimal.ZERO) > 0
                    && total.compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal calculatedQty = total.divide(
                        price,
                        0,
                        java.math.RoundingMode.HALF_UP);

                item.setBillQuantity(calculatedQty.intValue());
            }

            if (item.getBillQuantity() == null) {
                item.setBillQuantity(0);
            }
        }
    }

    private List<InventoryBillProductPreview> matchProductsWithDatabase(
            List<InventoryBillProductPreview> extractedProducts) {

        if (extractedProducts == null || extractedProducts.isEmpty()) {
            return new ArrayList<>();
        }

        List<Product> dbProducts = productRepository.findByActiveTrue();

        for (InventoryBillProductPreview item : extractedProducts) {
            ProductMatch bestMatch = findBestMatch(item.getProductName(), dbProducts);

            if (bestMatch != null && bestMatch.score >= 85) {
                Product product = bestMatch.product;

                item.setProductId(String.valueOf(product.getId()));
                item.setMatchedProductId(String.valueOf(product.getId()));
                item.setMatchedProductName(product.getName());

                item.setDbCategoryId(product.getCategoryId());
                item.setDbSubCategoryId(product.getSubCategoryId());

                // Do not overwrite AI/bill category if already extracted
                if (isBlank(item.getCategoryId())) {
                    item.setCategoryId(product.getCategoryId());
                }

                if (isBlank(item.getSubCategoryId())) {
                    item.setSubCategoryId(product.getSubCategoryId());
                }

                item.setStockQuantity(product.getStockQuantity() == null ? 0 : product.getStockQuantity());
                item.setImageUrl(product.getImageUrl());
                item.setExistingProduct(true);
                item.setConfidence(getConfidence(bestMatch.score));

                if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                    item.setPrice(product.getPrice());
                }

                if (isBlank(item.getUnit())) {
                    item.setUnit(product.getUnit());
                }

                item.setRemarks(
                        "Matched with existing product: "
                                + product.getName()
                                + ". Current stock: "
                                + item.getStockQuantity()
                                + ", bill quantity: "
                                + item.getBillQuantity()
                                + ", match score: "
                                + bestMatch.score
                                + "%");

            } else {
                item.setProductId("");
                item.setMatchedProductId("");
                item.setMatchedProductName("");
                item.setDbCategoryId("");
                item.setDbSubCategoryId("");
                item.setStockQuantity(0);
                item.setExistingProduct(false);

                if (isBlank(item.getConfidence())) {
                    item.setConfidence("AI");
                }

                item.setRemarks(
                        isBlank(item.getRemarks())
                                ? "AI extracted product. New product or admin selection required."
                                : item.getRemarks() + ". New product or admin selection required.");
            }
        }

        return extractedProducts;
    }

    private ProductMatch findBestMatch(String uploadedName, List<Product> dbProducts) {
        if (isBlank(uploadedName) || dbProducts == null || dbProducts.isEmpty()) {
            return null;
        }

        String uploaded = normalizeForMatch(uploadedName);

        if (!isValidNameForMatching(uploaded)) {
            return null;
        }

        ProductMatch bestMatch = null;

        for (Product product : dbProducts) {
            if (product == null || isBlank(product.getName())) {
                continue;
            }

            String dbName = normalizeForMatch(product.getName());

            // Important fix: ignore DB products like T, D, X, AB etc.
            if (!isValidNameForMatching(dbName)) {
                continue;
            }

            int score = calculateMatchScore(uploaded, dbName);

            if (bestMatch == null || score > bestMatch.score) {
                bestMatch = new ProductMatch(product, score);
            }
        }

        return bestMatch;
    }

    private int calculateMatchScore(String uploadedName, String dbName) {
        if (isBlank(uploadedName) || isBlank(dbName)) {
            return 0;
        }

        if (!isValidNameForMatching(uploadedName) || !isValidNameForMatching(dbName)) {
            return 0;
        }

        if (uploadedName.equals(dbName)) {
            return 100;
        }

        // Allow contains only for meaningful long names
        if (uploadedName.length() >= 5 && dbName.length() >= 5) {
            if (uploadedName.contains(dbName) || dbName.contains(uploadedName)) {
                return 90;
            }
        }

        String[] uploadedWords = uploadedName.split(" ");
        String[] dbWords = dbName.split(" ");

        int matchedWords = 0;
        int validUploadedWords = 0;

        for (String uploadedWord : uploadedWords) {
            if (uploadedWord.length() < 3) {
                continue;
            }

            validUploadedWords++;

            for (String dbWord : dbWords) {
                if (dbWord.length() < 3) {
                    continue;
                }

                if (uploadedWord.equals(dbWord)) {
                    matchedWords++;
                    break;
                }

                if (uploadedWord.length() >= 5 && dbWord.length() >= 5
                        && (uploadedWord.contains(dbWord) || dbWord.contains(uploadedWord))) {
                    matchedWords++;
                    break;
                }
            }
        }

        int wordScore = validUploadedWords == 0 ? 0 : (matchedWords * 100) / validUploadedWords;

        int distanceScore = levenshteinSimilarity(uploadedName, dbName);

        return Math.max(wordScore, distanceScore);
    }

    private boolean isValidNameForMatching(String name) {
        if (isBlank(name)) {
            return false;
        }

        String cleaned = normalizeForMatch(name);

        if (cleaned.length() < 3) {
            return false;
        }

        // reject single-word very short test data like T, D, AB
        if (cleaned.matches("^[a-z0-9]{1,2}$")) {
            return false;
        }

        return true;
    }

    private int levenshteinSimilarity(String a, String b) {
        if (isBlank(a) || isBlank(b)) {
            return 0;
        }

        int distance = levenshteinDistance(a, b);
        int maxLength = Math.max(a.length(), b.length());

        if (maxLength == 0) {
            return 0;
        }

        return (int) Math.round((1.0 - ((double) distance / maxLength)) * 100);
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }

        return dp[a.length()][b.length()];
    }

    private InventoryBillPreviewResponse buildResponse(
            String fileName,
            String fileType,
            List<InventoryBillProductPreview> products) {
        int totalProducts = products == null ? 0 : products.size();
        int matchedProducts = 0;
        int newProducts = 0;
        int failedProducts = 0;
        int totalBillQuantity = 0;

        if (products != null) {
            for (InventoryBillProductPreview product : products) {
                if (Boolean.TRUE.equals(product.getExistingProduct())) {
                    matchedProducts++;
                } else {
                    newProducts++;
                }

                if (product.getProductName() == null || product.getProductName().isBlank()) {
                    failedProducts++;
                }

                totalBillQuantity += product.getBillQuantity() == null ? 0 : product.getBillQuantity();
            }
        }

        InventoryBillPreviewResponse response = new InventoryBillPreviewResponse();

        response.setFileName(fileName);
        response.setFileType(fileType);
        response.setTotalProducts(totalProducts);
        response.setMatchedProducts(matchedProducts);
        response.setNewProducts(newProducts);
        response.setFailedProducts(failedProducts);
        response.setBillQuantity(totalBillQuantity);
        response.setProducts(products == null ? new ArrayList<>() : products);

        return response;
    }

    private String getFileType(String fileName) {
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            return "EXCEL";
        }

        if (fileName.endsWith(".pdf")) {
            return "PDF";
        }

        if (fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png")
                || fileName.endsWith(".webp")) {
            return "IMAGE";
        }

        throw new RuntimeException("Unsupported file type. Upload Excel, PDF, JPG, JPEG, PNG or WEBP.");
    }

    private String normalizeForMatch(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\b(nos|pcs|piece|qty|unit|kg|ltr|liter|metre|meter|mtr|mm|cm|inch|inches)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String getConfidence(int score) {
        if (score >= 90) {
            return "HIGH";
        }

        if (score >= 70) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void printPrettyJson(String title, Object data) {
        try {
            System.out.println("\n========== " + title + " ==========");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data));
            System.out.println("========== END " + title + " ==========\n");
        } catch (Exception e) {
            System.out.println("Unable to print JSON for: " + title);
            e.printStackTrace();
        }
    }

    private Long parseLong(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }

            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }
}