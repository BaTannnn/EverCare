package com.evercare.services.impl;

import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.enums.InvoiceStatus;
import com.evercare.enums.PrescriptionStatus;
import com.evercare.mappers.PrescriptionMapper;
import com.evercare.pojo.InventoryTransaction;
import com.evercare.pojo.Medicine;
import com.evercare.pojo.MedicineBatch;
import com.evercare.pojo.Prescription;
import com.evercare.pojo.PrescriptionItem;
import com.evercare.pojo.User;
import com.evercare.repositories.InventoryTransactionRepository;
import com.evercare.repositories.MedicineBatchRepository;
import com.evercare.repositories.PrescriptionRepository;
import com.evercare.services.PrescriptionService;
import com.evercare.utils.AuthSupport;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;
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
    private AuthSupport authSupport;

    @Override
    public List<PrescriptionResponse> getPrescriptions(Map<String, String> params) {
        List<Prescription> prescriptions = this.prescriptionRepo.getPrescriptions(params);
        Map<Long, Long> availableQuantityByMedicineId = getAvailableQuantities(prescriptions, Date.valueOf(LocalDate.now()));

        return prescriptions
                .stream()
                .map(prescription -> PrescriptionMapper.toResponse(prescription, availableQuantityByMedicineId))
                .toList();
    }

    @Override
    public PrescriptionResponse getPrescriptionById(Long id) {
        Prescription prescription = this.prescriptionRepo.getPrescriptionById(id);

        if (prescription == null) {
            throw new NoSuchElementException("Không tìm thấy đơn thuốc");
        }

        return PrescriptionMapper.toResponse(
                prescription,
                getAvailableQuantities(List.of(prescription), Date.valueOf(LocalDate.now()))
        );
    }

    @Override
    public PrescriptionResponse dispensePrescription(String username, Long id) {
        User user = this.authSupport.getCurrentUser(username);
        return dispensePrescription(user, id);
    }

    @Override
    public PrescriptionResponse dispensePrescription(User user, Long id) {
        Prescription prescription = this.prescriptionRepo.getPrescriptionByIdForUpdate(id);

        if (prescription == null) {
            throw new NoSuchElementException("Không tìm thấy đơn thuốc");
        }

        if (!PrescriptionStatus.PRESCRIBED.getCode().equalsIgnoreCase(prescription.getStatus())) {
            throw new IllegalStateException("Chỉ có thể cấp phát đơn thuốc trạng thái PRESCRIBED");
        }

        if (prescription.getPrescriptionItemSet() == null || prescription.getPrescriptionItemSet().isEmpty()) {
            throw new IllegalStateException("Đơn thuốc không có thuốc để cấp phát");
        }

        if (prescription.getMedicalRecordId() == null
                || !InvoiceStatus.PAID.getCode().equalsIgnoreCase(prescription.getMedicalRecordId().getPaymentStatus())) {
            throw new IllegalStateException("Bệnh nhân chưa thanh toán, chưa thể cấp phát thuốc");
        }

        Date today = Date.valueOf(LocalDate.now());
        Map<Long, List<MedicineBatch>> lockedBatchesByMedicineId = lockAndValidateDispensableBatches(prescription, today);
        java.util.Date now = new java.util.Date();

        List<PrescriptionItem> items = prescription.getPrescriptionItemSet()
                .stream()
                .sorted(Comparator.comparing(PrescriptionItem::getId))
                .toList();

        for (PrescriptionItem item : items) {
            dispenseItem(item, user, now, lockedBatchesByMedicineId);
        }

        prescription.setStatus(PrescriptionStatus.DISPENSED.getCode());
        prescription.setUpdatedAt(now);
        this.prescriptionRepo.updatePrescription(prescription);

        return PrescriptionMapper.toResponse(
                prescription,
                getAvailableQuantitiesFromLockedBatches(lockedBatchesByMedicineId)
        );
    }

    private Map<Long, List<MedicineBatch>> lockAndValidateDispensableBatches(Prescription prescription, Date today) {
        Map<Long, Long> requiredQuantityByMedicineId = getRequiredQuantitiesByMedicineId(prescription);
        Map<Long, List<MedicineBatch>> lockedBatchesByMedicineId = this.batchRepo
                .getDispensableBatchesByMedicineIdsForUpdate(
                        requiredQuantityByMedicineId.keySet().stream().toList(),
                        today
                )
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(batch -> batch.getMedicineId().getId()));

        for (Map.Entry<Long, Long> entry : requiredQuantityByMedicineId.entrySet()) {
            Long medicineId = entry.getKey();
            List<MedicineBatch> batches = lockedBatchesByMedicineId.getOrDefault(medicineId, List.of());
            long availableQuantity = batches.stream()
                    .map(MedicineBatch::getRemainingQuantity)
                    .filter(Objects::nonNull)
                    .mapToLong(Integer::longValue)
                    .sum();

            if (availableQuantity < entry.getValue()) {
                throw new IllegalStateException("Không đủ tồn kho còn hạn cho thuốc " + getMedicineName(prescription, medicineId));
            }
        }

        return lockedBatchesByMedicineId;
    }

    private Map<Long, Long> getRequiredQuantitiesByMedicineId(Prescription prescription) {
        Map<Long, Long> requiredQuantityByMedicineId = new TreeMap<>();

        for (PrescriptionItem item : prescription.getPrescriptionItemSet()) {
            Medicine medicine = item.getMedicineId();
            if (medicine == null || medicine.getId() == null) {
                throw new IllegalStateException("Đơn thuốc có dòng thuốc không hợp lệ");
            }

            int requiredQuantity = item.getQuantity() != null ? item.getQuantity() : 0;
            if (requiredQuantity <= 0) {
                throw new IllegalStateException("Số lượng thuốc phải lớn hơn 0");
            }

            requiredQuantityByMedicineId.merge(medicine.getId(), (long) requiredQuantity, Long::sum);
        }

        return requiredQuantityByMedicineId;
    }

    private Map<Long, Long> getAvailableQuantities(List<Prescription> prescriptions, Date today) {
        List<Long> medicineIds = prescriptions.stream()
                .filter(Objects::nonNull)
                .filter(prescription -> prescription.getPrescriptionItemSet() != null)
                .flatMap(prescription -> prescription.getPrescriptionItemSet().stream())
                .map(PrescriptionItem::getMedicineId)
                .filter(Objects::nonNull)
                .map(Medicine::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return this.batchRepo.getAvailableNonExpiredQuantitiesByMedicineIds(medicineIds, today);
    }

    private Map<Long, Long> getAvailableQuantitiesFromLockedBatches(Map<Long, List<MedicineBatch>> lockedBatchesByMedicineId) {
        Map<Long, Long> availableQuantityByMedicineId = new HashMap<>();

        for (Map.Entry<Long, List<MedicineBatch>> entry : lockedBatchesByMedicineId.entrySet()) {
            long total = entry.getValue()
                    .stream()
                    .map(MedicineBatch::getRemainingQuantity)
                    .filter(Objects::nonNull)
                    .mapToLong(Integer::longValue)
                    .sum();
            availableQuantityByMedicineId.put(entry.getKey(), total);
        }

        return availableQuantityByMedicineId;
    }

    private void dispenseItem(
            PrescriptionItem item,
            User user,
            java.util.Date now,
            Map<Long, List<MedicineBatch>> lockedBatchesByMedicineId
    ) {
        Medicine medicine = item.getMedicineId();
        int remainingNeed = item.getQuantity() != null ? item.getQuantity() : 0;
        List<MedicineBatch> batches = lockedBatchesByMedicineId.getOrDefault(medicine.getId(), List.of());

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

        if (remainingNeed > 0) {
            throw new IllegalStateException("Không đủ tồn kho còn hạn cho thuốc " + medicine.getName());
        }
    }

    private String getMedicineName(Prescription prescription, Long medicineId) {
        return prescription.getPrescriptionItemSet()
                .stream()
                .map(PrescriptionItem::getMedicineId)
                .filter(Objects::nonNull)
                .filter(medicine -> medicineId.equals(medicine.getId()))
                .map(Medicine::getName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("#" + medicineId);
    }
}
