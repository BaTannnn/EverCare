package com.evercare.repositories;

import com.evercare.pojo.Prescription;
import java.util.List;
import java.util.Map;

public interface PrescriptionRepository {
    List<Prescription> getPrescriptions(Map<String, String> params);

    Prescription getPrescriptionById(Long id);
}
