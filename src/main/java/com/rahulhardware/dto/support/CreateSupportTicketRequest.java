package com.rahulhardware.dto.support;

public class CreateSupportTicketRequest {

    private Long orderId;
    private String orderNumber;
    private String title;
    private String description;
    private String customerName;
    private String customerMobile;
    private String callbackNumber;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerMobile() {
        return customerMobile;
    }

    public void setCustomerMobile(String customerMobile) {
        this.customerMobile = customerMobile;
    }

    public String getCallbackNumber() {
        return callbackNumber;
    }

    public void setCallbackNumber(String callbackNumber) {
        this.callbackNumber = callbackNumber;
    }
}