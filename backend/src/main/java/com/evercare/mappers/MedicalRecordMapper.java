package com.evercare.mappers;

import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.pojo.MedicalRecord;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class MedicalRecordMapper {
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private MedicalRecordMapper() {
    }

    public static MedicalRecordResponse toResponse(MedicalRecord medicalRecord) {
        MedicalRecordResponse res = new MedicalRecordResponse();
        res.setId(medicalRecord.getId());
        res.setRecordCode(medicalRecord.getRecordCode());
        res.setVisitDate(format(medicalRecord.getVisitDate()));
        res.setChiefComplaint(medicalRecord.getChiefComplaint());
        res.setDiagnosis(medicalRecord.getDiagnosis());
        res.setTreatmentPlan(medicalRecord.getTreatmentPlan());
        res.setDoctorNote(medicalRecord.getDoctorNote());
        res.setPaymentStatus(medicalRecord.getPaymentStatus());
        res.setAppointmentId(medicalRecord.getAppointmentId() != null ? medicalRecord.getAppointmentId().getId() : null);
        res.setDoctorId(medicalRecord.getDoctorId() != null ? medicalRecord.getDoctorId().getId() : null);
        res.setPatientId(medicalRecord.getPatientId() != null ? medicalRecord.getPatientId().getId() : null);

        return res;
    }

    private static String format(Date value) {
        if (value == null) {
            return null;
        }

        return new SimpleDateFormat(DATETIME_PATTERN).format(value);
    }
}
