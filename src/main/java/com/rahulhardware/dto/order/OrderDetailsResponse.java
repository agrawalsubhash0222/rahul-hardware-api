package com.rahulhardware.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.rahulhardware.entity.OrderItem;

public class OrderDetailsResponse {

    private Long id;
    private String orderNumber;

    private String customerName;
    private String customerMobile;

    private String fullAddress;
    private String city;
    private String state;
    private String pinCode;

    private String paymentMethod;
    private String paymentStatus;
    private String orderStatus;

    private BigDecimal subtotalAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal platformFee;
    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

    private LocalDateTime confirmedAt;
    private LocalDateTime packedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime outForDeliveryAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    private String deliveryFailedReason;

    private LocalDateTime returnRequestedAt;
    private LocalDateTime returnApprovedAt;
    private LocalDateTime returnRejectedAt;
    private LocalDateTime pickupDoneAt;
    private LocalDateTime refundedAt;

    private LocalDateTime replacementRequestedAt;
    private LocalDateTime replacementApprovedAt;
    private LocalDateTime replacementRejectedAt;
    private LocalDateTime replacementDeliveredAt;

    private String cancellationReason;
    private String returnReason;
    private String replacementReason;

    private String returnRejectedReason;
    private String adminRemark;

    private String replacementRejectedReason;

    private String customerRemark;
    private String cancelledBy;

    private LocalDateTime returnCancelledAt;
    private LocalDateTime replacementCancelledAt;
    private String returnCancelReason;
    private String replacementCancelReason;

    private List<OrderItem> items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public BigDecimal getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(BigDecimal deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(BigDecimal platformFee) {
        this.platformFee = platformFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public LocalDateTime getPackedAt() {
        return packedAt;
    }

    public void setPackedAt(LocalDateTime packedAt) {
        this.packedAt = packedAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public LocalDateTime getOutForDeliveryAt() {
        return outForDeliveryAt;
    }

    public void setOutForDeliveryAt(LocalDateTime outForDeliveryAt) {
        this.outForDeliveryAt = outForDeliveryAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getDeliveryFailedReason() {
        return deliveryFailedReason;
    }

    public void setDeliveryFailedReason(String deliveryFailedReason) {
        this.deliveryFailedReason = deliveryFailedReason;
    }

    public LocalDateTime getReturnRequestedAt() {
        return returnRequestedAt;
    }

    public void setReturnRequestedAt(LocalDateTime returnRequestedAt) {
        this.returnRequestedAt = returnRequestedAt;
    }

    public LocalDateTime getReturnApprovedAt() {
        return returnApprovedAt;
    }

    public void setReturnApprovedAt(LocalDateTime returnApprovedAt) {
        this.returnApprovedAt = returnApprovedAt;
    }

    public LocalDateTime getReturnRejectedAt() {
        return returnRejectedAt;
    }

    public void setReturnRejectedAt(LocalDateTime returnRejectedAt) {
        this.returnRejectedAt = returnRejectedAt;
    }

    public LocalDateTime getPickupDoneAt() {
        return pickupDoneAt;
    }

    public void setPickupDoneAt(LocalDateTime pickupDoneAt) {
        this.pickupDoneAt = pickupDoneAt;
    }

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }

    public LocalDateTime getReplacementRequestedAt() {
        return replacementRequestedAt;
    }

    public void setReplacementRequestedAt(LocalDateTime replacementRequestedAt) {
        this.replacementRequestedAt = replacementRequestedAt;
    }

    public LocalDateTime getReplacementApprovedAt() {
        return replacementApprovedAt;
    }

    public void setReplacementApprovedAt(LocalDateTime replacementApprovedAt) {
        this.replacementApprovedAt = replacementApprovedAt;
    }

    public LocalDateTime getReplacementRejectedAt() {
        return replacementRejectedAt;
    }

    public void setReplacementRejectedAt(LocalDateTime replacementRejectedAt) {
        this.replacementRejectedAt = replacementRejectedAt;
    }

    public LocalDateTime getReplacementDeliveredAt() {
        return replacementDeliveredAt;
    }

    public void setReplacementDeliveredAt(LocalDateTime replacementDeliveredAt) {
        this.replacementDeliveredAt = replacementDeliveredAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public String getReplacementReason() {
        return replacementReason;
    }

    public void setReplacementReason(String replacementReason) {
        this.replacementReason = replacementReason;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getReturnRejectedReason() {
        return returnRejectedReason;
    }

    public void setReturnRejectedReason(String returnRejectedReason) {
        this.returnRejectedReason = returnRejectedReason;
    }

    public String getAdminRemark() {
        return adminRemark;
    }

    public void setAdminRemark(String adminRemark) {
        this.adminRemark = adminRemark;
    }

    public String getReplacementRejectedReason() {
        return replacementRejectedReason;
    }

    public void setReplacementRejectedReason(String replacementRejectedReason) {
        this.replacementRejectedReason = replacementRejectedReason;
    }

    public String getCustomerRemark() {
        return customerRemark;
    }

    public void setCustomerRemark(String customerRemark) {
        this.customerRemark = customerRemark;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public LocalDateTime getReturnCancelledAt() {
        return returnCancelledAt;
    }

    public void setReturnCancelledAt(LocalDateTime returnCancelledAt) {
        this.returnCancelledAt = returnCancelledAt;
    }

    public LocalDateTime getReplacementCancelledAt() {
        return replacementCancelledAt;
    }

    public void setReplacementCancelledAt(LocalDateTime replacementCancelledAt) {
        this.replacementCancelledAt = replacementCancelledAt;
    }

    public String getReturnCancelReason() {
        return returnCancelReason;
    }

    public void setReturnCancelReason(String returnCancelReason) {
        this.returnCancelReason = returnCancelReason;
    }

    public String getReplacementCancelReason() {
        return replacementCancelReason;
    }

    public void setReplacementCancelReason(String replacementCancelReason) {
        this.replacementCancelReason = replacementCancelReason;
    }
}