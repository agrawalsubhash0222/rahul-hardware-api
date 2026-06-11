package com.rahulhardware.dto.inventory;

import java.util.List;

public class ConfirmInventoryBillRequest {

    private List<InventoryBillProductPreview> products;

    public List<InventoryBillProductPreview> getProducts() {
        return products;
    }

    public void setProducts(List<InventoryBillProductPreview> products) {
        this.products = products;
    }
}