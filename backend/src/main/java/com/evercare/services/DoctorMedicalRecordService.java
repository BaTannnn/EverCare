package com.evercare.services;

import com.evercare.dtos.request.UpdateMedicalRecordRequest;
import com.evercare.dtos.response.MedicalRecordResponse;

public interface DoctorMedicalRecordService {
    MedicalRecordResponse updateMedicalRecord(String username, Long recordId, UpdateMedicalRecordRequest request);
}
