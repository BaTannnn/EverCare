package com.evercare.services;

import com.evercare.dtos.request.PatientRequest;
import com.evercare.dtos.response.PatientResponse;

public interface PatientService {
    PatientResponse createProfile(PatientRequest request);
    PatientResponse getMyProfile();
    PatientResponse updateMyProfile(PatientRequest request);
}
