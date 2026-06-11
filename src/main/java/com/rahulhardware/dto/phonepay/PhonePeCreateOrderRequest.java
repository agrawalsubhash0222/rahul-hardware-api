package com.rahulhardware.dto.phonepay;

public class PhonePeCreateOrderRequest {
    private Double amount;
    private String merchantOrderId;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getMerchantOrderId() {
        return merchantOrderId;
    }

    public void setMerchantOrderId(String merchantOrderId) {
        this.merchantOrderId = merchantOrderId;
    }
}