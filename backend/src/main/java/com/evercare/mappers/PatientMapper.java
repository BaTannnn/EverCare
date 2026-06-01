package com.evercare.mappers;

import com.evercare.dtos.response.PatientResponse;
import com.evercare.pojo.Patient;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;

public class PatientMapper {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private PatientMapper() {
    }

    public static PatientResponse toResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

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
        List<String> missingFields = collectMissingFields(patient);
        response.setMissingFields(missingFields);
        response.setProfileComplete(missingFields.isEmpty());
        return response;
    }

    public static List<String> collectMissingFields(Patient patient) {
        List<String> missingFields = new ArrayList<>();

        if (patient == null) {
            missingFields.add("fullName");
            missingFields.add("phone");
            missingFields.add("gender");
            missingFields.add("dateOfBirth");
            missingFields.add("citizenId");
            missingFields.add("healthInsuranceNo");
            missingFields.add("address");
            missingFields.add("emergencyContactName");
            missingFields.add("emergencyContactPhone");
            missingFields.add("bloodType");
            missingFields.add("allergyNote");
            missingFields.add("medicalHistoryNote");
            return missingFields;
        }

        if (isBlank(patient.getFullName())) {
            missingFields.add("fullName");
        }
        if (isBlank(patient.getPhone())) {
            missingFields.add("phone");
        }
        if (isBlank(patient.getGender())) {
            missingFields.add("gender");
        }
        if (patient.getDateOfBirth() == null) {
            missingFields.add("dateOfBirth");
        }
        if (isBlank(patient.getCitizenId())) {
            missingFields.add("citizenId");
        }
        if (isBlank(patient.getHealthInsuranceNo())) {
            missingFields.add("healthInsuranceNo");
        }
        if (isBlank(patient.getAddress())) {
            missingFields.add("address");
        }
        if (isBlank(patient.getEmergencyContactName())) {
            missingFields.add("emergencyContactName");
        }
        if (isBlank(patient.getEmergencyContactPhone())) {
            missingFields.add("emergencyContactPhone");
        }
        if (isBlank(patient.getBloodType())) {
            missingFields.add("bloodType");
        }
        if (isBlank(patient.getAllergyNote())) {
            missingFields.add("allergyNote");
        }
        if (isBlank(patient.getMedicalHistoryNote())) {
            missingFields.add("medicalHistoryNote");
        }

        return missingFields;
    }

    private static String formatDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        synchronized (DATE_FORMAT) {
            return DATE_FORMAT.format(date);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
