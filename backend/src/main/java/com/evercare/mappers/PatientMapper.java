package com.evercare.mappers;

import com.evercare.dtos.response.PatientResponse;
import com.evercare.pojo.Patient;
import java.text.SimpleDateFormat;

public class PatientMapper {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private PatientMapper() {
    }

    public static PatientResponse toResponse(Patient patient) {
        PatientResponse response = new PatientResponse();
        response.setId(patient.getId());
        response.setPatientCode(patient.getPatientCode());
        response.setFullName(patient.getFullName());
        response.setGender(patient.getGender());
        response.setDateOfBirth(formatDate(patient.getDateOfBirth()));
        response.setPhone(patient.getPhone());
        response.setEmail(patient.getEmail());
        response.setCitizenId(patient.getCitizenId());
        response.setHealthInsuranceNo(patient.getHealthInsuranceNo());
        response.setAddress(patient.getAddress());
        response.setEmergencyContactName(patient.getEmergencyContactName());
        response.setEmergencyContactPhone(patient.getEmergencyContactPhone());
        response.setBloodType(patient.getBloodType());
        response.setAllergyNote(patient.getAllergyNote());
        response.setMedicalHistoryNote(patient.getMedicalHistoryNote());
        response.setActive(patient.getActive());
        return response;
    }

    private static String formatDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        synchronized (DATE_FORMAT) {
            return DATE_FORMAT.format(date);
        }
    }
}
