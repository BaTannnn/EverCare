package com.evercare.repositories;

import com.evercare.pojo.MedicalRecordService;
import java.util.List;
import java.util.Map;

public interface MedicalRecordServiceRepository {
    void addMedicalRecordService(MedicalRecordService medicalRecordService);

    List<MedicalRecordService> getServicesByMedicalRecordId(Long recordId);

    List<String> getPendingResultServiceNames(Long recordId);

    List<MedicalRecordService> getPendingTestRequests(Map<String, String> params);
}
