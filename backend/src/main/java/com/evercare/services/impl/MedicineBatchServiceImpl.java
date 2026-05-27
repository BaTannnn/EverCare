package com.evercare.services.impl;

import com.evercare.dtos.request.MedicineBatchImportRequest;
import com.evercare.dtos.response.MedicineBatchImportResponse;
import com.evercare.dtos.response.MedicineBatchResponse;
import com.evercare.mappers.MedicineBatchMapper;
import com.evercare.pojo.InventoryTransaction;
import com.evercare.pojo.Medicine;
import com.evercare.pojo.MedicineBatch;
import com.evercare.pojo.User;
import com.evercare.repositories.InventoryTransactionRepository;
import com.evercare.repositories.MedicineBatchRepository;
import com.evercare.repositories.MedicineRepository;
import com.evercare.services.MedicineBatchService;
import com.evercare.services.UserService;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MedicineBatchServiceImpl implements MedicineBatchService {
    @Autowired
    private MedicineBatchRepository batchRepo;

    @Autowired
    private InventoryTransactionRepository transactionRepo;

    @Autowired
    private MedicineRepository medicineRepo;

    @Autowired
    private UserService userService;

    @Override
    public MedicineBatchImportResponse importBatch(String username, MedicineBatchImportRequest request) {
        User user = this.userService.getUserByUsername(username);
        validateRequest(request);

        Medicine medicine = this.medicineRepo.getMedicineById(request.getMedicineId());
        if (medicine == null || Boolean.FALSE.equals(medicine.getActive())) {
            throw new NoSuchElementException("Không tìm thấy thuốc hoặc thuốc đã ngưng sử dụng");
        }

        LocalDate importDate = parseDate(request.getImportDate(), "Ngày nhập không hợp lệ");
        LocalDate expiryDate = parseDate(request.getExpiryDate(), "Hạn sử dụng không hợp lệ");

        if (!expiryDate.isAfter(importDate)) {
            throw new IllegalArgumentException("Hạn sử dụng phải lớn hơn ngày nhập");
        }

        java.util.Date now = new java.util.Date();
        MedicineBatch batch = new MedicineBatch();
        batch.setMedicineId(medicine);
        batch.setBatchCode(request.getBatchCode().trim().toUpperCase());
        batch.setImportDate(Date.valueOf(importDate));
        batch.setExpiryDate(Date.valueOf(expiryDate));
        batch.setQuantity(request.getQuantity());
        batch.setRemainingQuantity(request.getQuantity());
        batch.setImportPrice(request.getImportPrice());
        batch.setSupplierName(request.getSupplierName() != null ? request.getSupplierName().trim() : null);
        batch.setCreatedAt(now);
        batch.setUpdatedAt(now);
        batch.setActive(true);

        this.batchRepo.addBatch(batch);

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setMedicineId(medicine);
        transaction.setBatchId(batch);
        transaction.setCreatedBy(user);
        transaction.setTransactionType("IMPORT");
        transaction.setQuantity(request.getQuantity());
        transaction.setTransactionDate(now);
        transaction.setNote("Nhập kho lô " + batch.getBatchCode());

        this.transactionRepo.addTransaction(transaction);

        MedicineBatchImportResponse response = new MedicineBatchImportResponse();
        response.setBatch(MedicineBatchMapper.toResponse(batch));
        response.setInventoryTransactionId(transaction.getId());

        return response;
    }

    @Override
    public List<MedicineBatchResponse> getBatches(Map<String, String> params) {
        return this.batchRepo.getBatches(params)
                .stream()
                .map(MedicineBatchMapper::toResponse)
                .toList();
    }

    @Override
    public List<MedicineBatchResponse> getBatchesByMedicineId(Long medicineId) {
        Medicine medicine = this.medicineRepo.getMedicineById(medicineId);
        if (medicine == null) {
            throw new NoSuchElementException("Không tìm thấy thuốc");
        }

        return this.batchRepo.getBatchesByMedicineId(medicineId)
                .stream()
                .map(MedicineBatchMapper::toResponse)
                .toList();
    }

    private void validateRequest(MedicineBatchImportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu nhập kho không hợp lệ");
        }

        if (request.getMedicineId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn thuốc");
        }

        if (request.getBatchCode() == null || request.getBatchCode().isBlank()) {
            throw new IllegalArgumentException("Mã lô không được để trống");
        }

        request.setBatchCode(request.getBatchCode().trim().toUpperCase());
        if (this.batchRepo.existsByBatchCode(request.getBatchCode())) {
            throw new IllegalArgumentException("Mã lô đã tồn tại");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        }

        if (request.getImportPrice() == null) {
            request.setImportPrice(BigDecimal.ZERO);
        }

        if (request.getImportPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá nhập không được âm");
        }
    }

    private LocalDate parseDate(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(message + ", định dạng đúng là yyyy-MM-dd");
        }
    }
}
