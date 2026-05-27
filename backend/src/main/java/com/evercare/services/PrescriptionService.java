package com.evercare.services;

import com.evercare.dtos.response.PrescriptionResponse;
import java.util.List;
import java.util.Map;

public interface PrescriptionService {
    List<PrescriptionResponse> getPrescriptions(Map<String, String> params);

    PrescriptionResponse getPrescriptionById(Long id);
}
