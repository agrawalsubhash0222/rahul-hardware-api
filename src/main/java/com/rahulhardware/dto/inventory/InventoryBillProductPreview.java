package com.rahulhardware.dto.inventory;

import java.math.BigDecimal;

public class InventoryBillProductPreview {

    private String categoryId;
    private String categoryName;

    private String subCategoryId;
    private String subCategoryName;

    private String productId;
    private String productName;

    private String description;

    private String productDetails;
    private String hsnSac;

    private String matchedProductId;
    private String matchedProductName;

    private String dbCategoryId;
    private String dbSubCategoryId;

    private Integer stockQuantity;
    private Integer billQuantity;

    private BigDecimal totalAmount;
    private BigDecimal price;

    private String unit;
    private String imageUrl;

    private String confidence;
    private String remarks;

    private Boolean existingProduct;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(String subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDetails() {
        return productDetails;
    }

    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }

    public String getHsnSac() {
        return hsnSac;
    }

    public void setHsnSac(String hsnSac) {
        this.hsnSac = hsnSac;
    }

    public String getMatchedProductId() {
        return matchedProductId;
    }

    public void setMatchedProductId(String matchedProductId) {
        this.matchedProductId = matchedProductId;
    }

    public String getMatchedProductName() {
        return matchedProductName;
    }

    public void setMatchedProductName(String matchedProductName) {
        this.matchedProductName = matchedProductName;
    }

    public String getDbCategoryId() {
        return dbCategoryId;
    }

    public void setDbCategoryId(String dbCategoryId) {
        this.dbCategoryId = dbCategoryId;
    }

    public String getDbSubCategoryId() {
        return dbSubCategoryId;
    }

    public void setDbSubCategoryId(String dbSubCategoryId) {
        this.dbSubCategoryId = dbSubCategoryId;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getBillQuantity() {
        return billQuantity;
    }

    public void setBillQuantity(Integer billQuantity) {
        this.billQuantity = billQuantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Boolean getExistingProduct() {
        return existingProduct;
    }

    public void setExistingProduct(Boolean existingProduct) {
        this.existingProduct = existingProduct;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}