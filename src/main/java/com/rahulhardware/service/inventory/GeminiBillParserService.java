package com.rahulhardware.service.inventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahulhardware.dto.inventory.AiBillItem;
import com.rahulhardware.dto.inventory.InventoryBillProductPreview;

@Service
public class GeminiBillParserService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    @Value("${gemini.fallback.model:gemini-2.5-flash-lite}")
    private String fallbackModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.builder().build();

    public List<InventoryBillProductPreview> extractProductsFromBill(MultipartFile file) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                throw new RuntimeException("Gemini API key missing. Set GEMINI_API_KEY environment variable.");
            }

            String mimeType = detectMimeType(file);
            String base64File = Base64.getEncoder().encodeToString(file.getBytes());

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "role", "user",
                                    "parts", List.of(
                                            Map.of("text", buildPrompt()),
                                            Map.of(
                                                    "inline_data", Map.of(
                                                            "mime_type", mimeType,
                                                            "data", base64File))))),
                    "generationConfig", Map.of(
                            "temperature", 0,
                            "responseMimeType", "application/json"));

            String response = callGeminiWithRetry(requestBody);
            String json = extractTextFromGeminiResponse(response);

            List<AiBillItem> aiItems = objectMapper.readValue(
                    json,
                    new TypeReference<List<AiBillItem>>() {
                    });

            printPrettyJson("FINAL AI EXTRACTED JSON", aiItems);

            return convertToPreview(aiItems);

        } catch (Exception e) {
            throw new RuntimeException("Gemini bill parsing failed: " + e.getMessage(), e);
        }
    }

    private String callGeminiWithRetry(Map<String, Object> requestBody) {
        List<String> models = new ArrayList<>();
        models.add(model);

        if (fallbackModel != null && !fallbackModel.isBlank() && !fallbackModel.equals(model)) {
            models.add(fallbackModel);
        }

        RuntimeException lastException = null;

        for (String selectedModel : models) {
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                            + selectedModel
                            + ":generateContent";

                    return restClient.post()
                            .uri(url)
                            .header("x-goog-api-key", apiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(requestBody)
                            .retrieve()
                            .body(String.class);

                } catch (Exception e) {
                    lastException = new RuntimeException(
                            "Gemini model failed: " + selectedModel + ", attempt: " + attempt,
                            e);

                    if (attempt < 3) {
                        sleepQuietly(1000L * attempt);
                    }
                }
            }
        }

        throw new RuntimeException(
                "Gemini is temporarily unavailable. Please try again after some time.",
                lastException);
    }

    private String buildPrompt() {
        return """
                You are a highly accurate Indian GST invoice and bill product extraction agent.

                Read the attached invoice/bill carefully.

                Return ONLY valid JSON array. No markdown. No explanation.

                JSON format:
                [
                  {
                    "productName": "string",
                    "description": "string",
                    "categoryName": "string",
                    "subCategoryName": "string",
                    "hsnCode": "string",
                    "quantity": 0,
                    "unit": "string",
                    "unitPrice": 0,
                    "taxPercent": 0,
                    "totalAmount": 0
                  }
                ]

                Critical table reading rules:
                - Read product rows column by column from left to right.
                - In invoice tables, columns usually appear like:
                  ITEM / DESCRIPTION / QTY / UNIT PRICE / TOTAL
                - Do not skip the QTY column.
                - If row is: Product XYZ 15 150.00 2250.00
                  then quantity = 15, unitPrice = 150.00, totalAmount = 2250.00
                - If row is: Product ABC 1 75.00 75.00
                  then quantity = 1, unitPrice = 75.00, totalAmount = 75.00
                - If quantity is visually present in Qty / Quantity column, never return 0.
                - If quantity is missing but totalAmount and unitPrice exist, calculate:
                  quantity = totalAmount / unitPrice
                - Never put quantity inside productName.

                Rules:
                - Extract only product line items from the item table.
                - Product name must come from Item Description / Description / Product / Particulars column.
                - Do not include HSN/SAC in productName.
                - Do not include quantity, unit, price, tax, or amount in productName.
                - quantity must come from Qty / Quantity column.
                - unitPrice must come from Rate / List Price / Unit Price column.
                - taxPercent must come from Tax / GST column.
                - totalAmount must come from Amount / Total column.
                - Ignore company name, address, GSTIN, invoice number, bank details, subtotal, grand total, terms, footer.
                - If value is missing, use empty string or 0.
                - Never guess products that are not visible in the bill.
                """;
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromGeminiResponse(String response) throws Exception {
        Map<String, Object> root = objectMapper.readValue(
                response,
                new TypeReference<Map<String, Object>>() {
                });

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) root.get("candidates");

        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("No response from Gemini.");
        }

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

        if (parts == null || parts.isEmpty()) {
            throw new RuntimeException("Gemini returned empty content.");
        }

        return String.valueOf(parts.get(0).get("text"))
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private List<InventoryBillProductPreview> convertToPreview(List<AiBillItem> aiItems) {
        List<InventoryBillProductPreview> products = new ArrayList<>();

        if (aiItems == null) {
            return products;
        }

        for (AiBillItem item : aiItems) {
            if (item.getProductName() == null || item.getProductName().isBlank()) {
                continue;
            }

            InventoryBillProductPreview dto = new InventoryBillProductPreview();

            dto.setCategoryId("");
            dto.setCategoryName(item.getCategoryName() == null ? "" : item.getCategoryName().trim());
            dto.setSubCategoryId("");
            dto.setSubCategoryName(item.getSubCategoryName() == null ? "" : item.getSubCategoryName().trim());
            dto.setProductId("");
            dto.setProductDetails(item.getDescription());
            dto.setHsnSac(item.getHsnCode());
            dto.setMatchedProductId("");
            dto.setProductName(item.getProductName().trim());
            dto.setStockQuantity(0);
            dto.setBillQuantity(item.getQuantity() == null ? 0 : item.getQuantity());
            dto.setPrice(item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice());
            dto.setTotalAmount(item.getTotalAmount() == null ? BigDecimal.ZERO : item.getTotalAmount());
            dto.setUnit(item.getUnit() == null || item.getUnit().isBlank() ? "PCS" : item.getUnit());
            dto.setImageUrl("");
            dto.setExistingProduct(false);
            dto.setConfidence("AI");
            dto.setRemarks(
                    "AI extracted. HSN: "
                            + safe(item.getHsnCode())
                            + ", Tax: "
                            + safe(item.getTaxPercent())
                            + ", Total: "
                            + safe(item.getTotalAmount()));

            products.add(dto);
        }

        return products;
    }

    private String detectMimeType(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();

        if (name.endsWith(".pdf")) {
            return "application/pdf";
        }

        if (name.endsWith(".png")) {
            return "image/png";
        }

        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        if (name.endsWith(".webp")) {
            return "image/webp";
        }

        if (name.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }

        if (name.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        }

        throw new RuntimeException("Unsupported file type. Upload Excel, PDF, JPG, JPEG, PNG or WEBP.");
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}