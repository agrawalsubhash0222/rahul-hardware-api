package com.rahulhardware.controller.inventory;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rahulhardware.entity.InventoryStock;
import com.rahulhardware.repository.InventoryStockRepository;

@RestController
@RequestMapping("/api/admin/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryStockRepository inventoryRepo;

    public InventoryController(InventoryStockRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    @GetMapping
    public List<InventoryStock> getAllStock() {
        return inventoryRepo.findByActiveTrueOrderByProductNameAsc();
    }

    @GetMapping("/low-stock")
    public List<InventoryStock> getLowStock() {
        return inventoryRepo.findByCurrentStockLessThanAndActiveTrue(8);
    }

    @GetMapping("/out-of-stock")
    public List<InventoryStock> getOutOfStock() {
        return inventoryRepo.findByCurrentStockAndActiveTrue(0);
    }

    @GetMapping("/search")
    public List<InventoryStock> searchStock(@RequestParam String keyword) {
        return inventoryRepo.findByProductNameContainingIgnoreCaseAndActiveTrue(keyword);
    }

    @PostMapping
    public InventoryStock addStock(@RequestBody InventoryStock stock) {
        if (inventoryRepo.existsByProductId(stock.getProductId())) {
            throw new RuntimeException("Inventory already exists for product id: " + stock.getProductId());
        }
        return inventoryRepo.save(stock);
    }

    @PutMapping("/{id}")
    public InventoryStock updateStock(
            @PathVariable Long id,
            @RequestBody InventoryStock request
    ) {
        InventoryStock stock = inventoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        stock.setCurrentStock(request.getCurrentStock());
        stock.setLowStockLimit(request.getLowStockLimit());
        stock.setPurchasePrice(request.getPurchasePrice());
        stock.setSellingPrice(request.getSellingPrice());
        stock.setUnit(request.getUnit());
        stock.setSupplierName(request.getSupplierName());
        stock.setLastUpdatedBy(request.getLastUpdatedBy());

        return inventoryRepo.save(stock);
    }

    @DeleteMapping("/{id}")
    public String removeStock(@PathVariable Long id) {
        InventoryStock stock = inventoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        stock.setActive(false);
        inventoryRepo.save(stock);

        return "Inventory removed successfully";
    }

    @PostMapping("/upload-preview")
    public String uploadInventoryDocument(@RequestParam("file") MultipartFile file) {
        // Phase 2: OCR / Excel / PDF parser will be added here.
        return "File received: " + file.getOriginalFilename();
    }
}