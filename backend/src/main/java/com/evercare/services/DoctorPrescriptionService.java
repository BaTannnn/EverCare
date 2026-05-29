package com.evercare.services;

import com.evercare.dtos.request.PrescriptionRequest;
import com.evercare.dtos.response.PrescriptionResponse;
import java.util.List;
import java.util.Map;

public interface DoctorPrescriptionService {
    List<PrescriptionResponse> getPrescriptions(String username, Map<String, String> params);

    PrescriptionResponse getPrescription(String username, Long prescriptionId);

    PrescriptionResponse createPrescription(String username, Long recordId, PrescriptionRequest request);

    PrescriptionResponse updatePrescription(String username, Long prescriptionId, PrescriptionRequest request);
}
