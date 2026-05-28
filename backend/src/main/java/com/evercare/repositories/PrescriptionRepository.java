package com.evercare.repositories;

import com.evercare.pojo.Prescription;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PrescriptionRepository {
    List<Prescription> getPrescriptions(Map<String, String> params);
    List<Prescription> getPrescriptionsByDoctorId(Long doctorId, Map<String, String> params);
    List<Prescription> getPrescriptionsByPatientId(Long patientId, String status, LocalDate from, LocalDate to, Map<String, String> params);

    Prescription getPrescriptionById(Long id);
    Prescription getPrescriptionByPatientIdAndId(Long patientId, Long id);
    Prescription getPrescriptionByMedicalRecordId(Long medicalRecordId);

    void addPrescription(Prescription prescription);
    void updatePrescription(Prescription prescription);
}
