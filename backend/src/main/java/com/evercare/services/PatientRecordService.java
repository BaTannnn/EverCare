package com.evercare.services;

import com.evercare.dtos.response.MedicalRecordDetailResponse;
import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.dtos.response.TestResultResponse;
import java.util.List;
import java.util.Map;

public interface PatientRecordService {
    List<MedicalRecordResponse> getMedicalRecordsByCurrentPatient(Map<String, String> params);

    MedicalRecordDetailResponse getMedicalRecordByCurrentPatient(Long recordId);

    List<TestResultResponse> getTestResultsByCurrentPatient(Map<String, String> params);

    List<PrescriptionResponse> getPrescriptionsByCurrentPatient(Map<String, String> params);

    PrescriptionResponse getPrescriptionByCurrentPatient(Long prescriptionId);
}
