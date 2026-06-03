package com.evercare.repositories;

import com.evercare.pojo.Patient;
import java.util.List;

public interface PatientRepository {
    Patient getPatientById(Long id);
    Patient getPatientByUserId(Long userId);
    Long getUserIdByPatientId(Long patientId);
    Patient getPatientByPhone(String phone);
    Patient getPatientByCitizenId(String citizenId);
    List<Patient> searchPatientsByKeyword(String keyword, int limit);
    boolean existsActiveByUserId(Long userId);
    boolean existsByCitizenId(String citizenId);
    boolean existsByHealthInsuranceNo(String healthInsuranceNo);
    Patient save(Patient patient);
    Patient update(Patient patient);
}
