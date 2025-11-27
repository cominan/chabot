package com.hung.chatbot.controller;

import com.hung.chatbot.entity.MaterialsInformation;
import com.hung.chatbot.repository.MaterialsInformationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/materials/transactions")
@RequiredArgsConstructor
public class MaterialTransactionController {

    private final MaterialsInformationRepository materialsInformationRepository;

    // 1. Basic transaction
    @PostMapping("/create-with-transaction")
    @Transactional
    public ResponseEntity<String> createWithTransaction(@RequestBody MaterialsInformation material) {
        material.setThoiGian(LocalDateTime.now());
        materialsInformationRepository.save(material);
        return ResponseEntity.ok("Material created successfully with transaction");
    }

    // 2. Transaction with checked exception (won't rollback by default)
    @PostMapping("/create-with-checked-exception")
    @Transactional
    public ResponseEntity<String> createWithCheckedException(@RequestBody MaterialsInformation material) throws Exception {
        material.setThoiGian(LocalDateTime.now());
        materialsInformationRepository.save(material);
        
        // This checked exception won't trigger rollback by default
        if (material.getTenVt() == null) {
            throw new Exception("Material name is required");
        }
        
        return ResponseEntity.ok("This won't be reached if exception occurs");
    }

    // 3. Transaction with runtime exception (rolls back by default)
    @PostMapping("/create-with-runtime-exception")
    @Transactional
    public ResponseEntity<String> createWithRuntimeException(@RequestBody MaterialsInformation material) {
        material.setThoiGian(LocalDateTime.now());
        materialsInformationRepository.save(material);
        
        // This will trigger rollback
        if (material.getSoLuong() == null || material.getSoLuong() <= 0) {
            throw new RuntimeException("Invalid quantity");
        }
        
        return ResponseEntity.ok("This won't be reached if exception occurs");
    }

    // 4. Transaction with no rollback for specific exception
    @PostMapping("/create-with-no-rollback")
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public ResponseEntity<String> createWithNoRollback(@RequestBody MaterialsInformation material) {
        material.setThoiGian(LocalDateTime.now());
        materialsInformationRepository.save(material);
        
        // This won't trigger rollback due to noRollbackFor
        if (material.getKhoPp() == null) {
            throw new IllegalArgumentException("Kho PP is required");
        }
        
        return ResponseEntity.ok("This won't be reached if exception occurs");
    }

    // 5. Nested transactions with propagation
    @PostMapping("/create-with-nested-transaction")
    @Transactional
    public ResponseEntity<String> createWithNestedTransaction(@RequestBody MaterialsInformation material) {
        material.setThoiGian(LocalDateTime.now());
        materialsInformationRepository.save(material);
        
        try {
            // This will run in a new transaction (REQUIRES_NEW)
            processInNewTransaction(material);
        } catch (Exception e) {
            // The outer transaction won't be affected by inner transaction's rollback
            return ResponseEntity.badRequest().body("Inner transaction failed but outer continued: " + e.getMessage());
        }
        
        return ResponseEntity.ok("Outer transaction completed successfully");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processInNewTransaction(MaterialsInformation material) {
        material.setStt(null); // Reset ID to force new record
        material.setTenVt(material.getTenVt() + " - PROCESSED");
        materialsInformationRepository.save(material);
        
        if (material.getSoLuong() < 10) {
            throw new RuntimeException("Quantity too low in inner transaction");
        }
    }

    // 6. Read-only transaction
    @GetMapping("/read-only")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MaterialsInformation>> getAllMaterialsReadOnly() {
        // This operation is optimized for read-only
        return ResponseEntity.ok(materialsInformationRepository.findAll());
    }

    /**
     * Tạo vật tư với cấu hình timeout
     * @param material Thông tin vật tư cần tạo
     * @return Thông báo kết quả
     */
    @Operation(
        summary = "Tạo vật tư với cấu hình timeout",
        description = "Minh họa việc sử dụng timeout cho transaction.\n" +
                     "Nếu thời gian thực thi vượt quá giá trị timeout, transaction sẽ bị rollback."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Tạo vật tư thành công",
        content = @Content(schema = @Schema(implementation = String.class))
    )
    @PostMapping("/create-with-timeout")
    @Transactional(timeout = 5) // 5 seconds timeout
    public ResponseEntity<String> createWithTimeout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Đối tượng Vật tư cần tạo",
                required = true,
                content = @Content(schema = @Schema(implementation = MaterialsInformation.class))
            )
            @RequestBody MaterialsInformation material) {
        material.setThoiGian(LocalDateTime.now());
        materialsInformationRepository.save(material);
        
        // Simulate long-running operation
        try {
            Thread.sleep(10000); // 10 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return ResponseEntity.ok("This will time out before reaching here");
    }
}
