package com.evercare.services.impl;

import com.evercare.dtos.request.PrescriptionItemRequest;
import com.evercare.dtos.request.PrescriptionRequest;
import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.enums.AppointmentStatus;
import com.evercare.enums.PrescriptionStatus;
import com.evercare.mappers.PrescriptionMapper;
import com.evercare.pojo.Appointment;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.Medicine;
import com.evercare.pojo.Prescription;
import com.evercare.pojo.PrescriptionItem;
import com.evercare.pojo.User;
import com.evercare.repositories.MedicalRecordRepository;
import com.evercare.repositories.MedicineBatchRepository;
import com.evercare.repositories.MedicineRepository;
import com.evercare.repositories.PrescriptionItemRepository;
import com.evercare.repositories.PrescriptionRepository;
import com.evercare.services.DoctorPrescriptionService;
import com.evercare.utils.AuthSupport;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorPrescriptionServiceImpl implements DoctorPrescriptionService {
    @Autowired
    private PrescriptionRepository prescriptionRepo;

    @Autowired
    private PrescriptionItemRepository prescriptionItemRepo;

    @Autowired
    private MedicalRecordRepository medicalRecordRepo;

    @Autowired
    private MedicineRepository medicineRepo;

    @Autowired
    private MedicineBatchRepository batchRepo;

    @Autowired
    private AuthSupport authSupport;

    @Override
    public List<PrescriptionResponse> getPrescriptions(String username, Map<String, String> params) {
        Doctor doctor = this.authSupport.requireCurrentDoctor(username);
        List<Prescription> prescriptions = this.prescriptionRepo.getPrescriptionsByDoctorId(doctor.getId(), params);
        Map<Long, Long> availableQuantityByMedicineId = getAvailableQuantities(prescriptions);

        return prescriptions
                .stream()
                .map(prescription -> PrescriptionMapper.toResponse(prescription, availableQuantityByMedicineId))
                .toList();
    }

    @Override
    public PrescriptionResponse getPrescription(String username, Long prescriptionId) {
        Doctor doctor = this.authSupport.requireCurrentDoctor(username);
        Prescription prescription = this.prescriptionRepo.getPrescriptionById(prescriptionId);

        if (prescription == null || Boolean.FALSE.equals(prescription.getActive())) {
            throw new NoSuchElementException("Không tìm thấy đơn thuốc");
        }

        validateOwnedMedicalRecord(doctor, prescription.getMedicalRecordId());

        return toResponse(prescription);
    }

    @Override
    public PrescriptionResponse createPrescription(String username, Long recordId, PrescriptionRequest request) {
        Doctor doctor = this.authSupport.requireCurrentDoctor(username);
        MedicalRecord medicalRecord = loadEditableMedicalRecord(doctor, recordId);

        if (this.prescriptionRepo.existsByMedicalRecordId(recordId)) {
            throw new IllegalStateException("Bệnh án này đã có đơn thuốc");
        }

        java.util.Date now = new java.util.Date();
        Prescription prescription = new Prescription();
        prescription.setPrescriptionCode(generatePrescriptionCode(recordId));
        prescription.setMedicalRecordId(medicalRecord);
        prescription.setDoctorId(doctor);
        prescription.setPatientId(medicalRecord.getPatientId());
        prescription.setPrescribedAt(now);
        prescription.setStatus(PrescriptionStatus.PRESCRIBED.getCode());
        prescription.setNote(clean(request != null ? request.getNote() : null));
        prescription.setCreatedAt(now);
        prescription.setUpdatedAt(now);
        prescription.setActive(true);

        Set<PrescriptionItem> items = buildItems(prescription, request, now);
        prescription.setPrescriptionItemSet(items);
        this.prescriptionRepo.addPrescription(prescription);

        return toResponse(prescription);
    }

    @Override
    public PrescriptionResponse updatePrescription(String username, Long prescriptionId, PrescriptionRequest request) {
        Doctor doctor = this.authSupport.requireCurrentDoctor(username);
        Prescription prescription = this.prescriptionRepo.getPrescriptionById(prescriptionId);

        if (prescription == null) {
            throw new NoSuchElementException("Không tìm thấy đơn thuốc");
        }

        MedicalRecord medicalRecord = prescription.getMedicalRecordId();
        validateOwnedMedicalRecord(doctor, medicalRecord);
        validateAppointmentEditable(medicalRecord);

        if (!PrescriptionStatus.PRESCRIBED.getCode().equalsIgnoreCase(prescription.getStatus())) {
            throw new IllegalStateException("Chỉ có thể cập nhật đơn thuốc trạng thái PRESCRIBED");
        }

        java.util.Date now = new java.util.Date();
        if (prescription.getPrescriptionItemSet() != null) {
            prescription.getPrescriptionItemSet().clear();
        }
        this.prescriptionItemRepo.deleteItemsByPrescriptionId(prescription.getId());

        Set<PrescriptionItem> items = buildItems(prescription, request, now);
        if (prescription.getPrescriptionItemSet() == null) {
            prescription.setPrescriptionItemSet(new HashSet<>());
        }
        for (PrescriptionItem item : items) {
            this.prescriptionItemRepo.addItem(item);
            prescription.getPrescriptionItemSet().add(item);
        }

        prescription.setNote(clean(request != null ? request.getNote() : null));
        prescription.setUpdatedAt(now);
        this.prescriptionRepo.updatePrescription(prescription);

        return toResponse(prescription);
    }

    private Set<PrescriptionItem> buildItems(Prescription prescription, PrescriptionRequest request, java.util.Date now) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn thuốc phải có ít nhất một thuốc");
        }

        Map<Long, Medicine> medicinesById = loadActiveMedicinesById(request);
        Set<PrescriptionItem> result = new HashSet<>();
        for (PrescriptionItemRequest itemRequest : request.getItems()) {
            if (itemRequest == null || itemRequest.getMedicineId() == null) {
                throw new IllegalArgumentException("Vui lòng chọn thuốc");
            }

            Medicine medicine = medicinesById.get(itemRequest.getMedicineId());
            if (medicine == null) {
                throw new IllegalArgumentException("Thuốc không tồn tại hoặc đã ngưng sử dụng");
            }

            int quantity = itemRequest.getQuantity() != null ? itemRequest.getQuantity() : 0;
            if (quantity <= 0) {
                throw new IllegalArgumentException("Số lượng thuốc phải lớn hơn 0");
            }

            PrescriptionItem item = new PrescriptionItem();
            item.setPrescriptionId(prescription);
            item.setMedicineId(medicine);
            item.setQuantity(quantity);
            item.setUnitPrice(medicine.getUnitPrice());
            item.setDosage(clean(itemRequest.getDosage()));
            item.setFrequency(clean(itemRequest.getFrequency()));
            item.setDuration(clean(itemRequest.getDuration()));
            item.setInstruction(clean(itemRequest.getInstruction()));
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            item.setActive(true);
            result.add(item);
        }

        return result;
    }

    private Map<Long, Medicine> loadActiveMedicinesById(PrescriptionRequest request) {
        List<Long> medicineIds = request.getItems()
                .stream()
                .filter(Objects::nonNull)
                .map(PrescriptionItemRequest::getMedicineId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Medicine> medicinesById = new HashMap<>();
        for (Medicine medicine : this.medicineRepo.getActiveMedicinesByIds(medicineIds)) {
            medicinesById.put(medicine.getId(), medicine);
        }

        return medicinesById;
    }

    private MedicalRecord loadEditableMedicalRecord(Doctor doctor, Long recordId) {
        MedicalRecord medicalRecord = this.medicalRecordRepo.getMedicalRecordById(recordId);
        if (medicalRecord == null || Boolean.FALSE.equals(medicalRecord.getActive())) {
            throw new NoSuchElementException("Không tìm thấy bệnh án");
        }

        validateOwnedMedicalRecord(doctor, medicalRecord);
        validateAppointmentEditable(medicalRecord);

        return medicalRecord;
    }

    private void validateOwnedMedicalRecord(Doctor doctor, MedicalRecord medicalRecord) {
        if (medicalRecord == null
                || medicalRecord.getDoctorId() == null
                || medicalRecord.getPatientId() == null
                || medicalRecord.getAppointmentId() == null) {
            throw new IllegalStateException("Bệnh án thiếu thông tin bác sĩ, bệnh nhân hoặc lịch hẹn");
        }

        if (!doctor.getId().equals(medicalRecord.getDoctorId().getId())) {
            throw new SecurityException("Chỉ bác sĩ phụ trách bệnh án mới được kê đơn");
        }
    }

    private void validateAppointmentEditable(MedicalRecord medicalRecord) {
        Appointment appointment = medicalRecord.getAppointmentId();
        if (AppointmentStatus.COMPLETED.getCode().equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Không thể kê đơn khi lịch khám đã COMPLETED");
        }

        if (!AppointmentStatus.IN_PROGRESS.getCode().equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Chỉ có thể kê đơn khi lịch khám đang IN_PROGRESS");
        }
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        return PrescriptionMapper.toResponse(prescription, getAvailableQuantities(List.of(prescription)));
    }

    private Map<Long, Long> getAvailableQuantities(List<Prescription> prescriptions) {
        Date today = Date.valueOf(LocalDate.now());
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

    private String generatePrescriptionCode(Long recordId) {
        return String.format("PRE%012d", recordId);
    }

    private String clean(String value) {
        return value != null ? value.trim() : null;
    }
}
