package com.evercare.services.impl;

import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.mappers.PrescriptionMapper;
import com.evercare.pojo.InventoryTransaction;
import com.evercare.pojo.Invoice;
import com.evercare.pojo.Medicine;
import com.evercare.pojo.MedicineBatch;
import com.evercare.pojo.Prescription;
import com.evercare.pojo.PrescriptionItem;
import com.evercare.pojo.User;
import com.evercare.repositories.InventoryTransactionRepository;
import com.evercare.repositories.InvoiceRepository;
import com.evercare.repositories.MedicineBatchRepository;
import com.evercare.repositories.PrescriptionRepository;
import com.evercare.services.PrescriptionService;
import com.evercare.services.UserService;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {
    @Autowired
    private PrescriptionRepository prescriptionRepo;

    @Autowired
    private MedicineBatchRepository batchRepo;

    @Autowired
    private InventoryTransactionRepository transactionRepo;

    @Autowired
    private InvoiceRepository invoiceRepo;

    @Autowired
    private UserService userService;

    @Override
    public List<PrescriptionResponse> getPrescriptions(Map<String, String> params) {
        Date today = Date.valueOf(LocalDate.now());

        return this.prescriptionRepo.getPrescriptions(params)
                .stream()
                .map(p -> PrescriptionMapper.toResponse(
                        p,
                        medicineId -> this.batchRepo.getAvailableNonExpiredQuantityByMedicineId(medicineId, today)
                ))
                .toList();
    }

    @Override
    public PrescriptionResponse getPrescriptionById(Long id) {
        Prescription prescription = this.prescriptionRepo.getPrescriptionById(id);

        if (prescription == null) {
            throw new NoSuchElementException("Không tìm thấy đơn thuốc");
        }

        Date today = Date.valueOf(LocalDate.now());
        return PrescriptionMapper.toResponse(
                prescription,
                medicineId -> this.batchRepo.getAvailableNonExpiredQuantityByMedicineId(medicineId, today)
        );
    }

    @Override
    public PrescriptionResponse dispensePrescription(String username, Long id) {
        User user = this.userService.getUserByUsername(username);
        Prescription prescription = this.prescriptionRepo.getPrescriptionById(id);

        if (prescription == null) {
            throw new NoSuchElementException("Không tìm thấy đơn thuốc");
        }

        if (!"PRESCRIBED".equalsIgnoreCase(prescription.getStatus())) {
            throw new IllegalStateException("Chỉ có thể cấp phát đơn thuốc trạng thái PRESCRIBED");
        }

        if (prescription.getPrescriptionItemSet() == null || prescription.getPrescriptionItemSet().isEmpty()) {
            throw new IllegalStateException("Đơn thuốc không có thuốc để cấp phát");
        }

        if (prescription.getMedicalRecordId() == null
                || !"PAID".equalsIgnoreCase(prescription.getMedicalRecordId().getPaymentStatus())) {
            throw new IllegalStateException("Bệnh nhân chưa thanh toán, chưa thể cấp phát thuốc");
        }

        Date today = Date.valueOf(LocalDate.now());
        validateEnoughStock(prescription, today);
        java.util.Date now = new java.util.Date();
        BigDecimal totalMedicineAmount = BigDecimal.ZERO;

        List<PrescriptionItem> items = prescription.getPrescriptionItemSet()
                .stream()
                .sorted(Comparator.comparing(PrescriptionItem::getId))
                .toList();

        for (PrescriptionItem item : items) {
            totalMedicineAmount = totalMedicineAmount.add(calculateItemAmount(item));
            dispenseItem(item, user, now, today);
        }

        prescription.setStatus("DISPENSED");
        prescription.setUpdatedAt(now);
        this.prescriptionRepo.updatePrescription(prescription);
        updateInvoiceMedicineAmount(prescription, totalMedicineAmount, now);

        return PrescriptionMapper.toResponse(
                prescription,
                medicineId -> this.batchRepo.getAvailableNonExpiredQuantityByMedicineId(medicineId, today)
        );
    }

    private void validateEnoughStock(Prescription prescription, Date today) {
        for (PrescriptionItem item : prescription.getPrescriptionItemSet()) {
            Medicine medicine = item.getMedicineId();
            if (medicine == null) {
                throw new IllegalStateException("Đơn thuốc có dòng thuốc không hợp lệ");
            }

            int requiredQuantity = item.getQuantity() != null ? item.getQuantity() : 0;
            if (requiredQuantity <= 0) {
                throw new IllegalStateException("Số lượng thuốc phải lớn hơn 0");
            }

            Long availableQuantity = this.batchRepo.getAvailableNonExpiredQuantityByMedicineId(medicine.getId(), today);
            if (availableQuantity < requiredQuantity) {
                throw new IllegalStateException("Không đủ tồn kho còn hạn cho thuốc " + medicine.getName());
            }
        }
    }

    private void dispenseItem(PrescriptionItem item, User user, java.util.Date now, Date today) {
        Medicine medicine = item.getMedicineId();
        int remainingNeed = item.getQuantity();
        List<MedicineBatch> batches = this.batchRepo.getDispensableBatchesByMedicineId(medicine.getId(), today);

        for (MedicineBatch batch : batches) {
            if (remainingNeed <= 0) {
                break;
            }

            int batchRemaining = batch.getRemainingQuantity() != null ? batch.getRemainingQuantity() : 0;
            int exportedQuantity = Math.min(batchRemaining, remainingNeed);

            if (exportedQuantity <= 0) {
                continue;
            }

            batch.setRemainingQuantity(batchRemaining - exportedQuantity);
            batch.setUpdatedAt(now);
            this.batchRepo.updateBatch(batch);

            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setMedicineId(medicine);
            transaction.setBatchId(batch);
            transaction.setPrescriptionItemId(item);
            transaction.setCreatedBy(user);
            transaction.setTransactionType("PRESCRIPTION_EXPORT");
            transaction.setQuantity(exportedQuantity);
            transaction.setTransactionDate(now);
            transaction.setNote("Cấp phát đơn " + item.getPrescriptionId().getPrescriptionCode());
            this.transactionRepo.addTransaction(transaction);

            remainingNeed -= exportedQuantity;
        }
    }

    private BigDecimal calculateItemAmount(PrescriptionItem item) {
        BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
        int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private void updateInvoiceMedicineAmount(Prescription prescription, BigDecimal totalMedicineAmount, java.util.Date now) {
        if (prescription.getMedicalRecordId() == null) {
            return;
        }

        Invoice invoice = this.invoiceRepo.getInvoiceByMedicalRecordId(prescription.getMedicalRecordId().getId());
        if (invoice == null) {
            return;
        }

        BigDecimal serviceAmount = invoice.getTotalServiceAmount() != null ? invoice.getTotalServiceAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO;

        invoice.setTotalMedicineAmount(totalMedicineAmount);
        invoice.setTotalAmount(serviceAmount.add(totalMedicineAmount).subtract(discountAmount));
        invoice.setUpdatedAt(now);
        this.invoiceRepo.updateInvoice(invoice);
    }
}
