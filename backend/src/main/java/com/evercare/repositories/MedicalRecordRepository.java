package com.evercare.repositories;

import com.evercare.pojo.MedicalRecord;

public interface MedicalRecordRepository {
    MedicalRecord getMedicalRecordById(Long recordId);

    void addMedicalRecord(MedicalRecord medicalRecord);

    void updateMedicalRecord(MedicalRecord medicalRecord);
}
