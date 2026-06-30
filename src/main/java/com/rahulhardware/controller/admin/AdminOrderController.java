package com.rahulhardware.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahulhardware.dto.admin.AdminOrderDetailsResponse;
import com.rahulhardware.dto.admin.AdminOrderResponse;
import com.rahulhardware.service.admin.AdminOrderService;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin("*")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public List<AdminOrderResponse> getAllOrders() {
        return adminOrderService.getAllOrders();
    }

    @GetMapping("/{orderId}")
    public AdminOrderDetailsResponse getOrderDetails(@PathVariable Long orderId) {
        return adminOrderService.getOrderDetails(orderId);
    }

    @PatchMapping("/{orderId}/status")
    public AdminOrderDetailsResponse updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        String reason = body.get("reason");
        String adminRemark = body.get("adminRemark");
        String customerRemark = body.get("customerRemark");

        boolean markPaymentPaid = Boolean.parseBoolean(
                body.getOrDefault("markPaymentPaid", "false"));

        return adminOrderService.updateOrderStatus(
                orderId,
                status,
                reason,
                adminRemark,
                customerRemark,
                markPaymentPaid);
    }

    @PatchMapping("/{orderId}/payment/paid")
    public AdminOrderDetailsResponse markCodPaymentPaid(@PathVariable Long orderId) {
        return adminOrderService.markCodPaymentPaid(orderId);
    }

    private String getStringValue(Map<String, Object> request, String key) {
        Object value = request.get(key);

        if (value == null) {
            return null;
        }

        String text = String.valueOf(value).trim();

        if (text.isEmpty() || "null".equalsIgnoreCase(text) || "undefined".equalsIgnoreCase(text)) {
            return null;
        }

        return text;
    }

    private boolean getBooleanValue(Map<String, Object> request, String key) {
        Object value = request.get(key);

        if (value == null) {
            return false;
        }

        return Boolean.parseBoolean(String.valueOf(value));
    }
}