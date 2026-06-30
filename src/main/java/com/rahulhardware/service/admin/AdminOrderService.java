package com.rahulhardware.service.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rahulhardware.dto.admin.AdminOrderDetailsResponse;
import com.rahulhardware.dto.admin.AdminOrderItemResponse;
import com.rahulhardware.dto.admin.AdminOrderResponse;
import com.rahulhardware.entity.CustomerOrder;
import com.rahulhardware.entity.OrderItem;
import com.rahulhardware.entity.OrderPayment;
import com.rahulhardware.entity.OrderStatusHistory;
import com.rahulhardware.entity.UserAddress;
import com.rahulhardware.repository.CustomerOrderRepository;
import com.rahulhardware.repository.OrderPaymentRepository;
import com.rahulhardware.repository.OrderStatusHistoryRepository;
import com.rahulhardware.repository.UserAddressRepository;

@Service
public class AdminOrderService {

    private static final List<String> DELIVERY_FLOW = List.of(
            "PLACED",
            "CONFIRMED",
            "PACKED",
            "SHIPPED",
            "OUT_FOR_DELIVERY",
            "DELIVERED");
    private static final Set<String> CANCEL_ALLOWED_FROM = Set.of(
            "PLACED",
            "CONFIRMED",
            "PACKED");
    private static final Set<String> VALID_STATUS = Set.of(
            "PLACED",
            "CONFIRMED",
            "PACKED",
            "SHIPPED",
            "OUT_FOR_DELIVERY",
            "DELIVERED",
            "CANCELLED",
            "DELIVERY_FAILED",
            "RETURN_REQUESTED",
            "RETURN_APPROVED",
            "RETURN_REJECTED",
            "PICKUP_DONE",
            "REFUNDED",
            "REPLACEMENT_REQUESTED",
            "REPLACEMENT_APPROVED",
            "REPLACEMENT_REJECTED",
            "REPLACEMENT_DELIVERED",
            "RETURN_CANCELLED",
            "REPLACEMENT_CANCELLED");

    private final CustomerOrderRepository customerOrderRepository;

    private final UserAddressRepository userAddressRepository;

    private final OrderPaymentRepository orderPaymentRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public AdminOrderService(
            CustomerOrderRepository customerOrderRepository,
            UserAddressRepository userAddressRepository,
            OrderPaymentRepository orderPaymentRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.customerOrderRepository = customerOrderRepository;
        this.userAddressRepository = userAddressRepository;
        this.orderPaymentRepository = orderPaymentRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    public List<AdminOrderResponse> getAllOrders() {
        return customerOrderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToAdminOrderResponse)
                .collect(Collectors.toList());
    }

    public AdminOrderDetailsResponse getOrderDetails(Long orderId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return mapToAdminOrderDetailsResponse(order);
    }

    public AdminOrderDetailsResponse updateOrderStatus(
            Long orderId,
            String newStatus,
            String reason,
            String adminRemark,
            String customerRemark,
            boolean markPaymentPaid) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new RuntimeException("Status is required");
        }

        newStatus = newStatus.trim().toUpperCase();
        String currentStatus = normalize(order.getOrderStatus());
        String oldStatus = currentStatus;

        validateStatusUpdate(currentStatus, newStatus, reason);

        LocalDateTime now = LocalDateTime.now();

        order.setOrderStatus(newStatus);

        if (adminRemark != null && !adminRemark.trim().isEmpty()) {
            order.setAdminRemark(adminRemark.trim());
        }

        if ("CONFIRMED".equals(newStatus)) {
            order.setConfirmedAt(now);
        }

        if ("PACKED".equals(newStatus)) {
            order.setPackedAt(now);
        }

        if ("SHIPPED".equals(newStatus)) {
            order.setShippedAt(now);
        }

        if ("OUT_FOR_DELIVERY".equals(newStatus)) {
            order.setOutForDeliveryAt(now);
        }

        if ("DELIVERED".equals(newStatus)) {
            order.setDeliveredAt(now);
        }

        if ("DELIVERY_FAILED".equals(newStatus)) {
            order.setDeliveryFailedReason(reason.trim());

            order.setPaymentStatus("PAYMENT_NOT_COLLECTED");

            OrderPayment payment = orderPaymentRepository.findByOrderId(order.getId())
                    .orElse(new OrderPayment());

            payment.setOrderId(order.getId());
            payment.setPaymentMethod(order.getPaymentMethod());
            payment.setPaymentStatus("PAYMENT_NOT_COLLECTED");
            payment.setPayableAmount(order.getTotalAmount());
            payment.setPaidAmount(java.math.BigDecimal.ZERO);
            payment.setTransactionId(null);
            payment.setPaidAt(null);

            orderPaymentRepository.save(payment);
        }

        if ("CANCELLED".equals(newStatus)) {
            order.setCancellationReason(reason);
            order.setAdminRemark(adminRemark);
            order.setCancelledBy("ADMIN");
            order.setCancelledAt(LocalDateTime.now());

            if ("COD".equalsIgnoreCase(order.getPaymentMethod())) {
                order.setPaymentStatus("PAYMENT_NOT_COLLECTED");

                OrderPayment payment = orderPaymentRepository.findByOrderId(order.getId())
                        .orElse(new OrderPayment());

                payment.setOrderId(order.getId());
                payment.setPaymentMethod(order.getPaymentMethod());
                payment.setPaymentStatus("PAYMENT_NOT_COLLECTED");
                payment.setPayableAmount(order.getTotalAmount());
                payment.setPaidAmount(java.math.BigDecimal.ZERO);
                payment.setTransactionId(null);
                payment.setPaidAt(null);

                orderPaymentRepository.save(payment);
            }
        }

        if ("RETURN_REQUESTED".equals(newStatus)) {
            order.setReturnReason(reason);
            order.setCustomerRemark(customerRemark);
            order.setReturnRequestedAt(LocalDateTime.now());
        }

        if ("REPLACEMENT_REQUESTED".equals(newStatus)) {
            order.setReplacementReason(reason.trim());
            order.setReplacementRequestedAt(now);
            order.setCustomerRemark(customerRemark);
        }

        if ("REFUNDED".equals(newStatus)) {
            order.setRefundedAt(now);
            order.setPaymentStatus("REFUNDED");

            OrderPayment payment = orderPaymentRepository.findByOrderId(order.getId())
                    .orElse(new OrderPayment());

            payment.setOrderId(order.getId());
            payment.setPaymentMethod(order.getPaymentMethod());
            payment.setPaymentStatus("REFUNDED");
            payment.setPayableAmount(order.getTotalAmount());
            payment.setPaidAmount(order.getTotalAmount());
            payment.setPaidAt(now);
            payment.setTransactionId(
                    "REFUND-" + (order.getOrderNumber() != null ? order.getOrderNumber() : order.getId()));

            orderPaymentRepository.save(payment);
        }

        if (markPaymentPaid && !"DELIVERY_FAILED".equals(newStatus) && !"CANCELLED".equals(newStatus)) {
            order.setPaymentStatus("PAID");

            OrderPayment payment = orderPaymentRepository.findByOrderId(order.getId())
                    .orElse(new OrderPayment());

            payment.setOrderId(order.getId());
            payment.setPaymentMethod(order.getPaymentMethod());
            payment.setPaymentStatus("PAID");
            payment.setPayableAmount(order.getTotalAmount());
            payment.setPaidAmount(order.getTotalAmount());
            payment.setTransactionId(
                    "COD-" + (order.getOrderNumber() != null ? order.getOrderNumber() : order.getId()));
            payment.setPaidAt(now);

            orderPaymentRepository.save(payment);
        }

        if ("RETURN_APPROVED".equals(newStatus)) {
            order.setReturnApprovedAt(now);
        }

        if ("RETURN_REJECTED".equals(newStatus)) {
            order.setOrderStatus("RETURN_REJECTED");
            order.setReturnRejectedAt(LocalDateTime.now());
            order.setReturnRejectedReason(reason);
            order.setAdminRemark(adminRemark);
        }

        if ("PICKUP_DONE".equals(newStatus)) {
            order.setPickupDoneAt(now);
        }

        if ("REPLACEMENT_APPROVED".equals(newStatus)) {
            order.setReplacementApprovedAt(now);
        }

        if ("REPLACEMENT_REJECTED".equals(newStatus)) {
            order.setOrderStatus("REPLACEMENT_REJECTED");
            order.setReplacementRejectedAt(LocalDateTime.now());
            order.setReplacementRejectedReason(reason);
            order.setAdminRemark(adminRemark);
        }

        if ("REPLACEMENT_DELIVERED".equals(newStatus)) {
            order.setReplacementDeliveredAt(now);
        }

        CustomerOrder savedOrder = customerOrderRepository.save(order);

        saveStatusHistory(
                savedOrder.getId(),
                oldStatus,
                savedOrder.getOrderStatus(),
                reason != null && !reason.trim().isEmpty() ? reason.trim() : adminRemark,
                "ADMIN");

        return mapToAdminOrderDetailsResponse(savedOrder);
    }

    public AdminOrderDetailsResponse markCodPaymentPaid(Long orderId) {

        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"COD".equalsIgnoreCase(order.getPaymentMethod())) {
            throw new RuntimeException(
                    "Only COD orders can be marked as paid");
        }

        order.setPaymentStatus("PAID");

        CustomerOrder savedOrder = customerOrderRepository.save(order);

        OrderPayment payment = orderPaymentRepository
                .findByOrderId(orderId)
                .orElse(new OrderPayment());

        payment.setOrderId(savedOrder.getId());

        payment.setPaymentMethod(savedOrder.getPaymentMethod());
        payment.setPaymentStatus("PAID");

        payment.setPayableAmount(savedOrder.getTotalAmount());
        payment.setPaidAmount(savedOrder.getTotalAmount());

        payment.setTransactionId(
                "COD-" + (savedOrder.getOrderNumber() != null
                        ? savedOrder.getOrderNumber()
                        : savedOrder.getId()));

        payment.setPaidAt(LocalDateTime.now());

        orderPaymentRepository.save(payment);

        return mapToAdminOrderDetailsResponse(savedOrder);
    }

    private void saveStatusHistory(
            Long orderId,
            String oldStatus,
            String newStatus,
            String remarks,
            String changedBy) {

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(orderId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setRemarks(remarks);
        history.setChangedBy(changedBy);
        history.setCreatedAt(LocalDateTime.now());

        orderStatusHistoryRepository.save(history);
    }

    private void validateStatusUpdate(String currentStatus, String newStatus, String reason) {
        if (!VALID_STATUS.contains(newStatus)) {
            throw new RuntimeException("Invalid order status: " + newStatus);
        }

        if (currentStatus.equals(newStatus)) {
            throw new RuntimeException("Order is already in this status");
        }

        if (isTerminalStatus(currentStatus)) {
            throw new RuntimeException("Cannot update order because current status is " + currentStatus);
        }

        if ("CANCELLED".equals(newStatus)) {
            if (!CANCEL_ALLOWED_FROM.contains(currentStatus)) {
                throw new RuntimeException("Only placed, confirmed, or packed orders can be cancelled");
            }

            requireReason(reason, "Cancellation reason is required");
            return;
        }

        if ("DELIVERY_FAILED".equals(newStatus)) {
            if (!"OUT_FOR_DELIVERY".equals(currentStatus)) {
                throw new RuntimeException("Only out-for-delivery orders can be marked delivery failed");
            }

            requireReason(reason, "Delivery failed reason is required");
            return;
        }

        if ("RETURN_REQUESTED".equals(newStatus)) {
            if (!"DELIVERED".equals(currentStatus)) {
                throw new RuntimeException("Return can be requested only after delivery");
            }

            requireReason(reason, "Return reason is required");
            return;
        }

        if ("RETURN_APPROVED".equals(newStatus)) {
            if (!"RETURN_REQUESTED".equals(currentStatus)) {
                throw new RuntimeException("Return must be requested before approval");
            }
            return;
        }

        if ("RETURN_REJECTED".equals(newStatus)) {
            if (!"RETURN_REQUESTED".equals(currentStatus)) {
                throw new RuntimeException("Return must be requested before rejection");
            }
            return;
        }

        if ("PICKUP_DONE".equals(newStatus)) {
            if (!"RETURN_APPROVED".equals(currentStatus)) {
                throw new RuntimeException("Return must be approved before pickup");
            }
            return;
        }

        if ("REFUNDED".equals(newStatus)) {
            if (!"PICKUP_DONE".equals(currentStatus)) {
                throw new RuntimeException("Pickup must be completed before refund");
            }
            return;
        }

        if ("REPLACEMENT_REQUESTED".equals(newStatus)) {
            if (!"DELIVERED".equals(currentStatus)) {
                throw new RuntimeException("Replacement can be requested only after delivery");
            }

            requireReason(reason, "Replacement reason is required");
            return;
        }

        if ("REPLACEMENT_APPROVED".equals(newStatus)) {
            if (!"REPLACEMENT_REQUESTED".equals(currentStatus)) {
                throw new RuntimeException("Replacement must be requested before approval");
            }
            return;
        }

        if ("REPLACEMENT_REJECTED".equals(newStatus)) {
            if (!"REPLACEMENT_REQUESTED".equals(currentStatus)) {
                throw new RuntimeException("Replacement must be requested before rejection");
            }
            return;
        }

        if ("REPLACEMENT_DELIVERED".equals(newStatus)) {
            if (!"REPLACEMENT_APPROVED".equals(currentStatus)) {
                throw new RuntimeException("Replacement must be approved first");
            }
            return;
        }

        validateForwardDeliveryFlow(currentStatus, newStatus);
    }

    private void validateForwardDeliveryFlow(String currentStatus, String newStatus) {
        int currentIndex = DELIVERY_FLOW.indexOf(currentStatus);
        int nextIndex = DELIVERY_FLOW.indexOf(newStatus);

        if (currentIndex < 0 || nextIndex < 0 || nextIndex != currentIndex + 1) {
            throw new RuntimeException("Invalid status movement from " + currentStatus + " to " + newStatus);
        }
    }

    private boolean isTerminalStatus(String status) {
        return Set.of(
                "CANCELLED",
                "DELIVERY_FAILED",
                "REFUNDED",
                "REPLACEMENT_DELIVERED",
                "RETURN_CANCELLED",
                "REPLACEMENT_CANCELLED").contains(status);
    }

    private void requireReason(String reason, String message) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException(message);
        }
    }

    private String normalize(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "PLACED";
        }

        return status.trim().toUpperCase();
    }

    private AdminOrderResponse mapToAdminOrderResponse(CustomerOrder order) {
        UserAddress address = findAddress(order.getAddressId());

        AdminOrderResponse response = new AdminOrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerName(address != null ? getCustomerName(address) : "Customer");
        response.setMobile(order.getUserMobile());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getOrderStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setAddressText(address != null ? buildAddressText(address) : null);

        return response;
    }

    private AdminOrderDetailsResponse mapToAdminOrderDetailsResponse(CustomerOrder order) {
        UserAddress address = findAddress(order.getAddressId());

        AdminOrderDetailsResponse response = new AdminOrderDetailsResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerName(address != null ? getCustomerName(address) : "Customer");
        response.setMobile(order.getUserMobile());
        response.setAddressText(address != null ? buildAddressText(address) : null);

        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setStatus(order.getOrderStatus());

        response.setCancellationReason(order.getCancellationReason());
        response.setReturnReason(order.getReturnReason());
        response.setReturnRejectedReason(order.getReturnRejectedReason());
        response.setReplacementReason(order.getReplacementReason());
        response.setReplacementRejectedReason(order.getReplacementRejectedReason());
        response.setAdminRemark(order.getAdminRemark());

        response.setCancelledAt(order.getCancelledAt());
        response.setReturnRequestedAt(order.getReturnRequestedAt());
        response.setReplacementRequestedAt(order.getReplacementRequestedAt());
        response.setRefundedAt(order.getRefundedAt());

        response.setSubtotalAmount(order.getSubtotalAmount());
        response.setDeliveryCharge(order.getDeliveryCharge());
        response.setPlatformFee(order.getPlatformFee());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());

        response.setConfirmedAt(order.getConfirmedAt());
        response.setPackedAt(order.getPackedAt());
        response.setShippedAt(order.getShippedAt());
        response.setOutForDeliveryAt(order.getOutForDeliveryAt());
        response.setDeliveredAt(order.getDeliveredAt());

        response.setDeliveryFailedReason(order.getDeliveryFailedReason());

        response.setCustomerRemark(order.getCustomerRemark());
        response.setCancelledBy(order.getCancelledBy());

        response.setReturnRequestedAt(order.getReturnRequestedAt());
        response.setReturnApprovedAt(order.getReturnApprovedAt());
        response.setReturnRejectedAt(order.getReturnRejectedAt());
        response.setPickupDoneAt(order.getPickupDoneAt());
        response.setRefundedAt(order.getRefundedAt());

        response.setReplacementRequestedAt(order.getReplacementRequestedAt());
        response.setReplacementApprovedAt(order.getReplacementApprovedAt());
        response.setReplacementRejectedAt(order.getReplacementRejectedAt());
        response.setReplacementDeliveredAt(order.getReplacementDeliveredAt());

        response.setReturnCancelledAt(order.getReturnCancelledAt());
        response.setReturnCancelReason(order.getReturnCancelReason());

        response.setReplacementCancelledAt(order.getReplacementCancelledAt());
        response.setReplacementCancelReason(order.getReplacementCancelReason());

        List<AdminOrderItemResponse> items = order.getItems()
                .stream()
                .map(this::mapItem)
                .collect(Collectors.toList());

        response.setItems(items);

        return response;
    }

    private AdminOrderItemResponse mapItem(OrderItem item) {
        AdminOrderItemResponse response = new AdminOrderItemResponse();
        response.setId(item.getId());
        response.setProductName(item.getProductName());
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());
        response.setTotalPrice(
                item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
        return response;
    }

    private UserAddress findAddress(Long addressId) {
        if (addressId == null)
            return null;
        return userAddressRepository.findById(addressId).orElse(null);
    }

    private String getCustomerName(UserAddress address) {
        String firstName = address.getFirstName() != null ? address.getFirstName() : "";
        String lastName = address.getLastName() != null ? address.getLastName() : "";

        String fullName = (firstName + " " + lastName).trim();

        return fullName.isEmpty() ? "Customer" : fullName;
    }

    private String buildAddressText(UserAddress address) {
        String fullAddress = address.getFullAddress() != null ? address.getFullAddress() : "";
        String city = address.getCity() != null ? address.getCity() : "";
        String state = address.getState() != null ? address.getState() : "";
        String pinCode = address.getPinCode() != null ? address.getPinCode() : "";

        return (fullAddress + ", " + city + ", " + state + " - " + pinCode)
                .replaceAll("(^,\\s*)|(,\\s*,)", ",")
                .trim();
    }
}