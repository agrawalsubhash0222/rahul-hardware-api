package com.rahulhardware.controller.order;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahulhardware.dto.order.CustomerOrderStatusUpdateRequest;
import com.rahulhardware.dto.order.OrderDetailsResponse;
import com.rahulhardware.dto.order.OrderRequest;
import com.rahulhardware.entity.CustomerOrder;
import com.rahulhardware.service.order.OrderService;

@RestController
@RequestMapping(value = "/api/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public CustomerOrder createOrder(@RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{mobile}")
    public List<CustomerOrder> getOrdersByMobile(@PathVariable String mobile) {
        return orderService.getOrdersByUserMobile(mobile);
    }

    @GetMapping("/{mobile}/{orderId}")
    public OrderDetailsResponse getOrderById(
            @PathVariable String mobile,
            @PathVariable Long orderId) {
        return orderService.getOrderById(mobile, orderId);
    }

    @GetMapping(value = "/{mobile}/{orderId}/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable String mobile,
            @PathVariable Long orderId) {

        byte[] pdfBytes = orderService.generateInvoicePdf(mobile, orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("invoice-order-" + orderId + ".pdf")
                        .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PatchMapping(
            value = "/{orderId}/status",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Object updateCustomerOrderStatus(
            @PathVariable Long orderId,
            @RequestBody CustomerOrderStatusUpdateRequest request) {

        return orderService.updateCustomerOrderStatus(
                orderId,
                request.getStatus(),
                request.getReason(),
                request.getCustomerRemark());
    }
}