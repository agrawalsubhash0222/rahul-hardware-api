package com.rahulhardware.dto.inventory;

import java.util.List;

public class InventoryBillPreviewResponse {

    private String fileName;

    private String fileType;

    private Integer totalProducts;

    private Integer matchedProducts;

    private Integer newProducts;

    private Integer failedProducts;

    private Integer billQuantity;

    private List<InventoryBillProductPreview> products;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Integer getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Integer totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Integer getMatchedProducts() {
        return matchedProducts;
    }

    public void setMatchedProducts(Integer matchedProducts) {
        this.matchedProducts = matchedProducts;
    }

    public Integer getNewProducts() {
        return newProducts;
    }

    public void setNewProducts(Integer newProducts) {
        this.newProducts = newProducts;
    }

    public Integer getFailedProducts() {
        return failedProducts;
    }

    public void setFailedProducts(Integer failedProducts) {
        this.failedProducts = failedProducts;
    }

    public List<InventoryBillProductPreview> getProducts() {
        return products;
    }

    public void setProducts(List<InventoryBillProductPreview> products) {
        this.products = products;
    }

    public Integer getBillQuantity() {
        return billQuantity;
    }

    public void setBillQuantity(Integer billQuantity) {
        this.billQuantity = billQuantity;
    }
}