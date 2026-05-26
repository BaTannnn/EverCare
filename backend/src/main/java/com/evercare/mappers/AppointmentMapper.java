package com.evercare.mappers;

import com.evercare.dtos.response.AppointmentPatientResponse;
import com.evercare.dtos.response.DoctorAppointmentResponse;
import com.evercare.enums.AppointmentStatus;
import com.evercare.pojo.Appointment;
import com.evercare.pojo.Patient;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class AppointmentMapper {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm";

    private AppointmentMapper() {
    }

    public static DoctorAppointmentResponse toDoctorResponse(Appointment appointment) {
        DoctorAppointmentResponse res = new DoctorAppointmentResponse();
        res.setId(appointment.getId());
        res.setAppointmentCode(appointment.getAppointmentCode());
        res.setAppointmentDate(format(appointment.getAppointmentDate(), DATE_PATTERN));
        res.setStartTime(format(appointment.getStartTime(), TIME_PATTERN));
        res.setEndTime(format(appointment.getEndTime(), TIME_PATTERN));
        res.setReason(appointment.getReason());
        res.setSymptomNote(appointment.getSymptomNote());
        res.setStatus(appointment.getStatus());
        res.setStatusLabel(AppointmentStatus.labelOf(appointment.getStatus()));
        res.setPatient(toPatientResponse(appointment.getPatientId()));

        return res;
    }

    private static AppointmentPatientResponse toPatientResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        AppointmentPatientResponse res = new AppointmentPatientResponse();
        res.setId(patient.getId());
        res.setPatientCode(patient.getPatientCode());
        res.setFullName(patient.getFullName());
        res.setGender(patient.getGender());
        res.setDateOfBirth(format(patient.getDateOfBirth(), DATE_PATTERN));
        res.setPhone(patient.getPhone());
        res.setEmail(patient.getEmail());

        return res;
    }

    private static String format(Date value, String pattern) {
        if (value == null) {
            return null;
        }

        return new SimpleDateFormat(pattern).format(value);
    }
}
