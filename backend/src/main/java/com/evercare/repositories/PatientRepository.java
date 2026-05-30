package com.evercare.repositories;

import com.evercare.pojo.Patient;

public interface PatientRepository {
    Patient getPatientById(Long id);
    Patient getPatientByUserId(Long userId);
    Patient getPatientByPhone(String phone);
    Patient getPatientByCitizenId(String citizenId);
    boolean existsActiveByUserId(Long userId);
    boolean existsByCitizenId(String citizenId);
    boolean existsByHealthInsuranceNo(String healthInsuranceNo);
    Patient save(Patient patient);
    Patient update(Patient patient);
}
