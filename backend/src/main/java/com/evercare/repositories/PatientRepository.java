package com.evercare.repositories;

import com.evercare.pojo.Patient;

public interface PatientRepository {
    Patient getPatientByUserId(Long userId);
    boolean existsActiveByUserId(Long userId);
    boolean existsByCitizenId(String citizenId);
    boolean existsByHealthInsuranceNo(String healthInsuranceNo);
    Patient save(Patient patient);
    Patient update(Patient patient);
}
