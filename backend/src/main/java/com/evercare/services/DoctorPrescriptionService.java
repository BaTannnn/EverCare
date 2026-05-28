package com.evercare.services;

import com.evercare.dtos.request.PrescriptionRequest;
import com.evercare.dtos.response.PrescriptionResponse;

public interface DoctorPrescriptionService {
    PrescriptionResponse createPrescription(String username, Long recordId, PrescriptionRequest request);

    PrescriptionResponse updatePrescription(String username, Long prescriptionId, PrescriptionRequest request);
}
