package com.evercare.mappers;

import com.evercare.dtos.response.PrescriptionItemResponse;
import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.Medicine;
import com.evercare.pojo.Patient;
import com.evercare.pojo.Prescription;
import com.evercare.pojo.PrescriptionItem;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public final class PrescriptionMapper {
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private PrescriptionMapper() {
    }

    public static PrescriptionResponse toResponse(
            Prescription prescription,
            Function<Long, Long> availableQuantityResolver
    ) {
        PrescriptionResponse res = new PrescriptionResponse();
        Doctor doctor = prescription.getDoctorId();
        Patient patient = prescription.getPatientId();
        MedicalRecord medicalRecord = prescription.getMedicalRecordId();

        res.setId(prescription.getId());
        res.setPrescriptionCode(prescription.getPrescriptionCode());
        res.setPrescribedAt(format(prescription.getPrescribedAt()));
        res.setStatus(prescription.getStatus());
        res.setNote(prescription.getNote());
        res.setMedicalRecordId(medicalRecord != null ? medicalRecord.getId() : null);
        res.setAppointmentId(medicalRecord != null && medicalRecord.getAppointmentId() != null
                ? medicalRecord.getAppointmentId().getId()
                : null);
        res.setDiagnosis(medicalRecord != null ? medicalRecord.getDiagnosis() : null);
        res.setPaymentStatus(medicalRecord != null ? medicalRecord.getPaymentStatus() : null);
        res.setDoctorId(doctor != null ? doctor.getId() : null);
        res.setDoctorName(doctor != null ? doctor.getFullName() : null);
        res.setPatientId(patient != null ? patient.getId() : null);
        res.setPatientCode(patient != null ? patient.getPatientCode() : null);
        res.setPatientName(patient != null ? patient.getFullName() : null);

        List<PrescriptionItemResponse> items = prescription.getPrescriptionItemSet() == null
                ? Collections.emptyList()
                : prescription.getPrescriptionItemSet()
                        .stream()
                        .map(item -> toItemResponse(item, availableQuantityResolver))
                        .toList();
        res.setItems(items);

        return res;
    }

    private static PrescriptionItemResponse toItemResponse(
            PrescriptionItem item,
            Function<Long, Long> availableQuantityResolver
    ) {
        PrescriptionItemResponse res = new PrescriptionItemResponse();
        Medicine medicine = item.getMedicineId();
        Long medicineId = medicine != null ? medicine.getId() : null;
        Long availableQuantity = medicineId != null && availableQuantityResolver != null
                ? availableQuantityResolver.apply(medicineId)
                : null;
        int requiredQuantity = item.getQuantity() != null ? item.getQuantity() : 0;

        res.setId(item.getId());
        res.setMedicineId(medicineId);
        res.setMedicineCode(medicine != null ? medicine.getMedicineCode() : null);
        res.setMedicineName(medicine != null ? medicine.getName() : null);
        res.setUnit(medicine != null ? medicine.getUnit() : null);
        res.setQuantity(item.getQuantity());
        res.setAvailableQuantity(availableQuantity);
        res.setEnoughStock(availableQuantity != null ? availableQuantity >= requiredQuantity : null);
        res.setUnitPrice(item.getUnitPrice());
        res.setDosage(item.getDosage());
        res.setFrequency(item.getFrequency());
        res.setDuration(item.getDuration());
        res.setInstruction(item.getInstruction());

        return res;
    }

    private static String format(Date value) {
        if (value == null) {
            return null;
        }

        return new SimpleDateFormat(DATETIME_PATTERN).format(value);
    }
}
