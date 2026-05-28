package com.evercare.repositories;

import com.evercare.pojo.MedicalRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MedicalRecordRepository {
    MedicalRecord getMedicalRecordById(Long recordId);
    List<MedicalRecord> getMedicalRecordsByPatientId(Long patientId, LocalDate from, LocalDate to, Map<String, String> params);
    MedicalRecord getMedicalRecordByPatientIdAndId(Long patientId, Long recordId);

    void addMedicalRecord(MedicalRecord medicalRecord);

    void updateMedicalRecord(MedicalRecord medicalRecord);
}
