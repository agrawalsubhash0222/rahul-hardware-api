package com.rahulhardware.controller.inventory;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rahulhardware.dto.inventory.ConfirmInventoryBillRequest;
import com.rahulhardware.dto.inventory.InventoryBillPreviewResponse;
import com.rahulhardware.dto.inventory.InventoryBillRequest;
import com.rahulhardware.service.inventory.InventoryBillService;

@RestController
@RequestMapping("/api/inventory-bills")
@CrossOrigin("*")
public class InventoryBillController {

    private final InventoryBillService inventoryBillService;

    public InventoryBillController(InventoryBillService inventoryBillService) {
        this.inventoryBillService = inventoryBillService;
    }

    @PostMapping("/preview")
    public ResponseEntity<InventoryBillPreviewResponse> previewBill(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(inventoryBillService.extractProducts(file));
    }

    @PostMapping("/save-products")
    public ResponseEntity<?> saveProducts(@RequestBody List<InventoryBillRequest> products) {
        inventoryBillService.saveProducts(products);
        return ResponseEntity.ok("Products saved successfully");
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirmInventoryBill(
            @RequestBody ConfirmInventoryBillRequest request) {
        inventoryBillService.confirmAndUpdateStock(request);
        return ResponseEntity.ok("Stock updated successfully");
    }
}