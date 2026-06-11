package com.rahulhardware.dto.phonepay;

public class PhonePeCreateOrderResponse {
    private String merchantOrderId;
    private String orderId;
    private String token;
    private String state;

    public PhonePeCreateOrderResponse(
            String merchantOrderId,
            String orderId,
            String token,
            String state
    ) {
        this.merchantOrderId = merchantOrderId;
        this.orderId = orderId;
        this.token = token;
        this.state = state;
    }

    public String getMerchantOrderId() {
        return merchantOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getToken() {
        return token;
    }

    public String getState() {
        return state;
    }
}