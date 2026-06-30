package com.rahulhardware.service.order;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.rahulhardware.dto.order.OrderDetailsResponse;
import com.rahulhardware.dto.order.OrderRequest;
import com.rahulhardware.entity.CustomerOrder;
import com.rahulhardware.entity.OrderItem;
import com.rahulhardware.entity.OrderPayment;
import com.rahulhardware.entity.OrderStatusHistory;
import com.rahulhardware.entity.Product;
import com.rahulhardware.entity.UserAddress;
import com.rahulhardware.entity.UserCartItem;
import com.rahulhardware.repository.CustomerOrderRepository;
import com.rahulhardware.repository.OrderPaymentRepository;
import com.rahulhardware.repository.OrderStatusHistoryRepository;
import com.rahulhardware.repository.ProductRepository;
import com.rahulhardware.repository.UserAddressRepository;
import com.rahulhardware.repository.UserCartItemRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final UserAddressRepository addressRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final UserCartItemRepository userCartItemRepository;
    private final ProductRepository productRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public OrderService(
            CustomerOrderRepository orderRepository,
            UserAddressRepository addressRepository,
            OrderPaymentRepository orderPaymentRepository,
            UserCartItemRepository userCartItemRepository,
            ProductRepository productRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.orderPaymentRepository = orderPaymentRepository;
        this.userCartItemRepository = userCartItemRepository;
        this.productRepository = productRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    @Transactional
    public CustomerOrder createOrder(OrderRequest request) {
        if (request.getUserMobile() == null || request.getUserMobile().isBlank()) {
            throw new RuntimeException("User mobile is required");
        }

        if (request.getAddressId() == null) {
            throw new RuntimeException("Address is required");
        }

        List<UserCartItem> cartItems = userCartItemRepository.findByUserMobile(request.getUserMobile());

        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        CustomerOrder order = new CustomerOrder();

        order.setUserMobile(request.getUserMobile());
        order.setAddressId(request.getAddressId());
        order.setPaymentMethod(defaultValue(request.getPaymentMethod(), "COD"));
        order.setPaymentStatus("PENDING");
        order.setOrderStatus("PLACED");

        BigDecimal subtotal = BigDecimal.ZERO;

        for (UserCartItem cartItem : cartItems) {
            Long productId = Long.valueOf(cartItem.getProductId());

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            BigDecimal price = defaultAmount(product.getPrice());
            Integer qty = cartItem.getQuantity() == null ? 1 : cartItem.getQuantity();
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(qty));

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImageUrl(product.getImageUrl());
            item.setUnit(product.getUnit());
            item.setPrice(price);
            item.setQuantity(qty);
            item.setTotalPrice(itemTotal);

            order.addItem(item);
            subtotal = subtotal.add(itemTotal);
        }

        BigDecimal deliveryCharge = subtotal.compareTo(BigDecimal.valueOf(999)) >= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(49);

        BigDecimal platformFee = BigDecimal.valueOf(5);
        BigDecimal total = subtotal.add(deliveryCharge).add(platformFee);

        order.setSubtotalAmount(subtotal);
        order.setDeliveryCharge(deliveryCharge);
        order.setPlatformFee(platformFee);
        order.setTotalAmount(total);

        System.out.println("========== CREATE ORDER DEBUG ==========");
        System.out.println("Mobile: " + request.getUserMobile());

        System.out.println("Request items:");
        if (request.getItems() != null) {
            request.getItems().forEach(i -> {
                System.out.println(
                        i.getProductId() + " | " +
                                i.getProductName() + " | Qty: " +
                                i.getQuantity());
            });
        }

        System.out.println("DB cart:");
        userCartItemRepository
                .findCartItemsWithProductDetails(request.getUserMobile())
                .forEach(c -> {
                    System.out.println(
                            c.getProductId() + " | " +
                                    c.getName() + " | Qty: " +
                                    c.getQuantity());
                });

        System.out.println("=======================================");

        CustomerOrder savedOrder = orderRepository.save(order);

        savePaymentStatus(
                savedOrder,
                "PENDING",
                BigDecimal.ZERO,
                null);

        saveStatusHistory(
                savedOrder.getId(),
                null,
                "PLACED",
                "Order placed by customer",
                "CUSTOMER");

        userCartItemRepository.deleteByUserMobile(request.getUserMobile());

        return savedOrder;
    }

    public List<CustomerOrder> getOrdersByUserMobile(String userMobile) {
        return orderRepository.findByUserMobileOrderByCreatedAtDesc(userMobile);
    }

    public OrderDetailsResponse getOrderById(String userMobile, Long orderId) {
        CustomerOrder order = orderRepository
                .findByIdAndUserMobile(orderId, userMobile)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        UserAddress address = addressRepository
                .findById(order.getAddressId())
                .orElse(null);

        OrderDetailsResponse response = new OrderDetailsResponse();

        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());

        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setOrderStatus(order.getOrderStatus());

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

        response.setCancelledAt(order.getCancelledAt());
        response.setCancellationReason(order.getCancellationReason());

        response.setDeliveryFailedReason(order.getDeliveryFailedReason());

        response.setCustomerRemark(order.getCustomerRemark());
        response.setCancelledBy(order.getCancelledBy());

        response.setReturnRequestedAt(order.getReturnRequestedAt());
        response.setReturnApprovedAt(order.getReturnApprovedAt());
        response.setReturnRejectedAt(order.getReturnRejectedAt());
        response.setPickupDoneAt(order.getPickupDoneAt());
        response.setRefundedAt(order.getRefundedAt());
        response.setReturnReason(order.getReturnReason());
        response.setReturnRejectedReason(order.getReturnRejectedReason());
        response.setAdminRemark(order.getAdminRemark());
        response.setReturnCancelledAt(order.getReturnCancelledAt());
        response.setReturnCancelReason(order.getReturnCancelReason());

        response.setReplacementRequestedAt(order.getReplacementRequestedAt());
        response.setReplacementApprovedAt(order.getReplacementApprovedAt());
        response.setReplacementRejectedAt(order.getReplacementRejectedAt());
        response.setReplacementDeliveredAt(order.getReplacementDeliveredAt());
        response.setReplacementReason(order.getReplacementReason());
        response.setReplacementRejectedReason(order.getReplacementRejectedReason());
        response.setReplacementCancelledAt(order.getReplacementCancelledAt());
        response.setReplacementCancelReason(order.getReplacementCancelReason());

        response.setItems(order.getItems());

        if (address != null) {
            response.setCustomerName(
                    (address.getFirstName() == null ? "" : address.getFirstName())
                            + " "
                            + (address.getLastName() == null ? "" : address.getLastName()));

            response.setCustomerMobile(address.getMobile());
            response.setFullAddress(address.getFullAddress());
            response.setCity(address.getCity());
            response.setState(address.getState());
            response.setPinCode(address.getPinCode());
        }

        return response;
    }

    public byte[] generateInvoicePdf(String userMobile, Long orderId) {
        OrderDetailsResponse order = getOrderById(userMobile, orderId);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            document.setMargins(28, 28, 28, 28);

            Color green = new DeviceRgb(0, 168, 107);
            Color dark = new DeviceRgb(15, 23, 42);
            Color muted = new DeviceRgb(100, 116, 139);
            Color lightBg = new DeviceRgb(248, 250, 252);
            Color border = new DeviceRgb(226, 232, 240);

            Table header = new Table(UnitValue.createPercentArray(new float[] { 2, 1 }))
                    .useAllAvailableWidth();

            Cell brandCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(new Paragraph("Rahul Hardware")
                            .setBold()
                            .setFontSize(24)
                            .setFontColor(dark))
                    .add(new Paragraph("Quality hardware delivered to your doorstep")
                            .setFontSize(10)
                            .setFontColor(muted));

            Cell invoiceCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .add(new Paragraph("TAX INVOICE")
                            .setBold()
                            .setFontSize(18)
                            .setFontColor(green))
                    .add(new Paragraph("Order #" + order.getId())
                            .setFontSize(11)
                            .setFontColor(muted));

            header.addCell(brandCell);
            header.addCell(invoiceCell);
            document.add(header);

            document.add(new Paragraph("\n"));

            Table meta = new Table(UnitValue.createPercentArray(new float[] { 1, 1, 1 }))
                    .useAllAvailableWidth()
                    .setBackgroundColor(lightBg)
                    .setBorder(new SolidBorder(border, 1));

            meta.addCell(infoCell("Order ID", safe(order.getOrderNumber()), dark, muted));
            meta.addCell(infoCell("Placed On", formatDate(order.getCreatedAt()), dark, muted));
            meta.addCell(infoCell("Status", safe(order.getOrderStatus()), green, muted));

            document.add(meta);
            document.add(new Paragraph("\n"));

            Table details = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }))
                    .useAllAvailableWidth();

            Cell customer = new Cell()
                    .setPadding(14)
                    .setBorder(new SolidBorder(border, 1))
                    .add(sectionTitle("Bill To", dark))
                    .add(new Paragraph(safe(order.getCustomerName())).setBold().setFontSize(12).setFontColor(dark))
                    .add(new Paragraph("Mobile: " + safe(order.getCustomerMobile())).setFontSize(10)
                            .setFontColor(muted))
                    .add(new Paragraph(safe(order.getFullAddress())).setFontSize(10).setFontColor(muted))
                    .add(new Paragraph(
                            safe(order.getCity()) + ", " +
                                    safe(order.getState()) + " - " +
                                    safe(order.getPinCode()))
                            .setFontSize(10)
                            .setFontColor(muted));

            Cell payment = new Cell()
                    .setPadding(14)
                    .setBorder(new SolidBorder(border, 1))
                    .add(sectionTitle("Payment Details", dark))
                    .add(new Paragraph("Method: " + safe(order.getPaymentMethod())).setFontSize(10).setFontColor(muted))
                    .add(new Paragraph("Payment Status: " + safe(order.getPaymentStatus())).setFontSize(10)
                            .setFontColor(muted))
                    .add(new Paragraph("Order Status: " + safe(order.getOrderStatus())).setFontSize(10)
                            .setFontColor(muted));

            details.addCell(customer);
            details.addCell(payment);
            document.add(details);

            document.add(new Paragraph("\n"));
            document.add(sectionTitle("Items Ordered", dark));

            Table itemsTable = new Table(UnitValue.createPercentArray(new float[] { 4, 1, 2, 2 }))
                    .useAllAvailableWidth();

            itemsTable.addHeaderCell(tableHeader("Product", green));
            itemsTable.addHeaderCell(tableHeader("Qty", green));
            itemsTable.addHeaderCell(tableHeader("Price", green));
            itemsTable.addHeaderCell(tableHeader("Total", green));

            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    itemsTable.addCell(tableBody(safe(item.getProductName())));
                    itemsTable.addCell(tableBody(String.valueOf(item.getQuantity())));
                    itemsTable.addCell(tableBody("Rs. " + amount(item.getPrice())));
                    itemsTable.addCell(tableBody("Rs. " + amount(item.getTotalPrice())));
                }
            }

            document.add(itemsTable);
            document.add(new Paragraph("\n"));

            Table bill = new Table(UnitValue.createPercentArray(new float[] { 2, 1 }))
                    .setWidth(UnitValue.createPercentValue(45))
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setBorder(new SolidBorder(border, 1));

            bill.addCell(summaryLabel("Subtotal", muted));
            bill.addCell(summaryValue("Rs. " + amount(order.getSubtotalAmount()), dark));

            bill.addCell(summaryLabel("Delivery Charge", muted));
            bill.addCell(summaryValue("Rs. " + amount(order.getDeliveryCharge()), dark));

            bill.addCell(summaryLabel("Platform Fee", muted));
            bill.addCell(summaryValue("Rs. " + amount(order.getPlatformFee()), dark));

            bill.addCell(new Cell()
                    .setPadding(10)
                    .setBackgroundColor(green)
                    .setBorder(Border.NO_BORDER)
                    .add(new Paragraph("Total Amount").setBold().setFontColor(ColorConstants.WHITE)));

            bill.addCell(new Cell()
                    .setPadding(10)
                    .setBackgroundColor(green)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .add(new Paragraph("Rs. " + amount(order.getTotalAmount()))
                            .setBold()
                            .setFontColor(ColorConstants.WHITE)));

            document.add(bill);

            document.add(new Paragraph("\n\nThank you for shopping with Rahul Hardware.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(11)
                    .setFontColor(muted));

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Unable to generate invoice PDF", e);
        }
    }

    public Object updateCustomerOrderStatus(
            Long orderId,
            String newStatus,
            String reason,
            String customerRemark) {

        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String currentStatus = order.getOrderStatus() == null
                ? "PLACED"
                : order.getOrderStatus().trim().toUpperCase();

        String oldStatus = currentStatus;

        newStatus = newStatus == null ? "" : newStatus.trim().toUpperCase();

        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Reason is required");
        }

        String finalReason = reason.trim();
        String finalCustomerRemark = customerRemark == null ? null : customerRemark.trim();
        LocalDateTime now = LocalDateTime.now();

        if ("CANCELLED".equals(newStatus)) {

            if (!currentStatus.equals("PLACED")
                    && !currentStatus.equals("CONFIRMED")
                    && !currentStatus.equals("PACKED")) {
                throw new RuntimeException("Order cannot be cancelled after shipping");
            }

            order.setOrderStatus("CANCELLED");
            order.setCancellationReason(finalReason);
            order.setCustomerRemark(finalCustomerRemark);
            order.setCancelledBy("CUSTOMER");
            order.setCancelledAt(now);

            if ("COD".equalsIgnoreCase(order.getPaymentMethod())) {
                order.setPaymentStatus("PAYMENT_NOT_COLLECTED");

                OrderPayment payment = orderPaymentRepository.findByOrderId(order.getId())
                        .orElse(new OrderPayment());

                payment.setOrderId(order.getId());
                payment.setPaymentMethod(order.getPaymentMethod());
                payment.setPaymentStatus("PAYMENT_NOT_COLLECTED");
                payment.setPayableAmount(order.getTotalAmount());
                payment.setPaidAmount(BigDecimal.ZERO);
                payment.setTransactionId(null);
                payment.setPaidAt(null);

                orderPaymentRepository.save(payment);
            }

        } else if ("RETURN_REQUESTED".equals(newStatus)) {

            if (!currentStatus.equals("DELIVERED")) {
                throw new RuntimeException("Return can be requested only after delivery");
            }

            order.setOrderStatus("RETURN_REQUESTED");
            order.setReturnReason(finalReason);
            order.setCustomerRemark(finalCustomerRemark);
            order.setReturnRequestedAt(now);

        } else if ("REPLACEMENT_REQUESTED".equals(newStatus)) {

            if (!currentStatus.equals("DELIVERED")) {
                throw new RuntimeException("Replacement can be requested only after delivery");
            }

            order.setOrderStatus("REPLACEMENT_REQUESTED");
            order.setReplacementReason(finalReason);
            order.setCustomerRemark(finalCustomerRemark);
            order.setReplacementRequestedAt(now);

        } else if ("RETURN_CANCELLED".equals(newStatus)) {

            if (!currentStatus.equals("RETURN_REQUESTED")) {
                throw new RuntimeException("Return request can be cancelled only before approval");
            }

            order.setOrderStatus("RETURN_CANCELLED");
            order.setReturnCancelledAt(now);
            order.setReturnCancelReason(finalReason);
            order.setCustomerRemark(finalCustomerRemark);

        } else if ("REPLACEMENT_CANCELLED".equals(newStatus)) {

            if (!currentStatus.equals("REPLACEMENT_REQUESTED")) {
                throw new RuntimeException("Replacement request can be cancelled only before approval");
            }

            order.setOrderStatus("REPLACEMENT_CANCELLED");
            order.setReplacementCancelledAt(now);
            order.setReplacementCancelReason(finalReason);
            order.setCustomerRemark(finalCustomerRemark);

        } else {
            throw new RuntimeException("Invalid customer action");
        }

        CustomerOrder savedOrder = orderRepository.save(order);

        saveStatusHistory(
                savedOrder.getId(),
                oldStatus,
                savedOrder.getOrderStatus(),
                finalReason,
                "CUSTOMER");

        return savedOrder;
    }

    private void savePaymentStatus(
            CustomerOrder order,
            String paymentStatus,
            BigDecimal paidAmount,
            LocalDateTime paidAt) {

        OrderPayment payment = orderPaymentRepository.findByOrderId(order.getId())
                .orElse(new OrderPayment());

        payment.setOrderId(order.getId());
        payment.setPaymentMethod(order.getPaymentMethod());
        payment.setPaymentStatus(paymentStatus);
        payment.setPayableAmount(order.getTotalAmount());
        payment.setPaidAmount(paidAmount == null ? BigDecimal.ZERO : paidAmount);
        payment.setPaidAt(paidAt);

        if (!"PAID".equals(paymentStatus)) {
            payment.setTransactionId(null);
        }

        if ("PAID".equals(paymentStatus)) {
            payment.setTransactionId(
                    "COD-" + (order.getOrderNumber() != null ? order.getOrderNumber() : order.getId()));
        }

        orderPaymentRepository.save(payment);
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

    private Cell infoCell(String label, String value, Color dark, Color muted) {
        return new Cell()
                .setPadding(12)
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph(label).setFontSize(9).setFontColor(muted))
                .add(new Paragraph(value).setBold().setFontSize(11).setFontColor(dark));
    }

    private Paragraph sectionTitle(String text, Color dark) {
        return new Paragraph(text)
                .setBold()
                .setFontSize(13)
                .setFontColor(dark);
    }

    private Cell tableHeader(String text, Color green) {
        return new Cell()
                .setPadding(9)
                .setBackgroundColor(green)
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE));
    }

    private Cell tableBody(String text) {
        return new Cell()
                .setPadding(9)
                .setBorder(new SolidBorder(new DeviceRgb(226, 232, 240), 1))
                .add(new Paragraph(text).setFontSize(10));
    }

    private Cell summaryLabel(String text, Color muted) {
        return new Cell()
                .setPadding(9)
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph(text).setFontSize(10).setFontColor(muted));
    }

    private Cell summaryValue(String text, Color dark) {
        return new Cell()
                .setPadding(9)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(text).setBold().setFontSize(10).setFontColor(dark));
    }

    private String amount(Object value) {
        if (value == null) {
            return "0.00";
        }

        return new BigDecimal(String.valueOf(value))
                .setScale(2, RoundingMode.HALF_UP)
                .toString();
    }

    private String formatDate(Object value) {
        return value == null ? "" : String.valueOf(value).replace("T", " ");
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}