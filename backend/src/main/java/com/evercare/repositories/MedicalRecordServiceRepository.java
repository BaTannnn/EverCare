package com.evercare.repositories;

import com.evercare.pojo.MedicalRecordService;
import java.util.List;

public interface MedicalRecordServiceRepository {
    void addMedicalRecordService(MedicalRecordService medicalRecordService);

    List<MedicalRecordService> getServicesByMedicalRecordId(Long recordId);

    List<MedicalRecordService> getPendingTestRequests();
}
