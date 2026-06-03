package com.evercare.services;

import com.evercare.dtos.request.PatientRequest;
import com.evercare.dtos.response.PatientResponse;
import java.util.List;

public interface PatientService {
    PatientResponse createProfile(PatientRequest request);
    PatientResponse getMyProfile();
    PatientResponse updateMyProfile(PatientRequest request);
    List<PatientResponse> searchForReceptionist(String keyword, Integer limit);
}
