package com.evercare.services;

import com.evercare.dtos.request.MedicalRecordServiceRequest;
import com.evercare.dtos.request.UpdateMedicalRecordRequest;
import com.evercare.dtos.response.MedicalRecordServiceResponse;
import com.evercare.dtos.response.MedicalRecordResponse;
import java.util.List;

public interface DoctorMedicalRecordService {
    MedicalRecordResponse updateMedicalRecord(String username, Long recordId, UpdateMedicalRecordRequest request);

    MedicalRecordServiceResponse addService(String username, Long recordId, MedicalRecordServiceRequest request);

    List<MedicalRecordServiceResponse> getServices(String username, Long recordId);
}
