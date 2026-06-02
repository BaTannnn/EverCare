package com.evercare.mappers;

import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.dtos.response.MedicalRecordDetailResponse;
import com.evercare.dtos.response.MedicalRecordServiceResponse;
import com.evercare.dtos.response.TestResultResponse;
import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.dtos.response.AppointmentPatientResponse;
import com.evercare.dtos.response.AppointmentResponse;
import com.evercare.dtos.response.DepartmentResponse;
import com.evercare.dtos.response.DoctorResponse;
import com.evercare.pojo.Appointment;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.MedicalRecordService;
import com.evercare.pojo.Patient;
import com.evercare.pojo.Prescription;
import com.evercare.pojo.TestResult;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.Department;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Collections;
import java.util.List;

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

    public static MedicalRecordDetailResponse toDetailResponse(
            MedicalRecord medicalRecord,
            List<MedicalRecordService> services,
            List<TestResult> testResults,
            Prescription prescription
    ) {
        MedicalRecordDetailResponse res = new MedicalRecordDetailResponse();
        res.setId(medicalRecord.getId());
        res.setRecordCode(medicalRecord.getRecordCode());
        res.setVisitDate(format(medicalRecord.getVisitDate()));
        res.setAppointmentCode(medicalRecord.getAppointmentId() != null ? medicalRecord.getAppointmentId().getAppointmentCode() : null);
        res.setDoctorName(medicalRecord.getDoctorId() != null ? medicalRecord.getDoctorId().getFullName() : null);
        res.setDepartmentName(
                medicalRecord.getDoctorId() != null && medicalRecord.getDoctorId().getDepartmentId() != null
                        ? medicalRecord.getDoctorId().getDepartmentId().getName()
                        : null
        );
        res.setChiefComplaint(medicalRecord.getChiefComplaint());
        res.setDiagnosis(medicalRecord.getDiagnosis());
        res.setTreatmentPlan(medicalRecord.getTreatmentPlan());
        res.setDoctorNote(medicalRecord.getDoctorNote());
        res.setPaymentStatus(medicalRecord.getPaymentStatus());
        res.setAppointment(toAppointmentResponse(medicalRecord.getAppointmentId()));
        res.setPatient(toPatientResponse(medicalRecord.getPatientId()));
        res.setDoctor(toDoctorResponse(medicalRecord.getDoctorId()));
        res.setDepartment(toDepartmentResponse(
                medicalRecord.getDoctorId() != null ? medicalRecord.getDoctorId().getDepartmentId() : null
        ));

        List<MedicalRecordServiceResponse> serviceResponses = services == null
                ? Collections.emptyList()
                : services.stream()
                        .map(service -> MedicalRecordServiceMapper.toSummaryResponse(service, getTestResultsForService(service, testResults)))
                        .toList();
        res.setServices(serviceResponses);

        List<TestResultResponse> testResultResponses = testResults == null
                ? Collections.emptyList()
                : testResults.stream().map(TestResultMapper::toResponse).toList();
        res.setTestResults(testResultResponses);
        res.setPrescription(prescription != null
                ? PrescriptionMapper.toResponse(prescription, (java.util.function.Function<Long, Long>) null)
                : null);

        return res;
    }

    private static List<TestResult> getTestResultsForService(MedicalRecordService service, List<TestResult> testResults) {
        if (service == null || testResults == null || testResults.isEmpty() || service.getServiceId() == null) {
            return Collections.emptyList();
        }

        return testResults.stream()
                .filter(tr -> tr.getServiceId() != null && service.getServiceId().getId().equals(tr.getServiceId().getId()))
                .toList();
    }

    private static AppointmentResponse toAppointmentResponse(Appointment appointment) {
        return appointment != null ? AppointmentMapper.toPatientResponse(appointment) : null;
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
        res.setDateOfBirth(format(patient.getDateOfBirth()));
        res.setPhone(patient.getPhone());
        res.setEmail(patient.getEmail());
        return res;
    }

    private static DoctorResponse toDoctorResponse(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        DoctorResponse res = new DoctorResponse();
        res.setId(doctor.getId());
        res.setDoctorCode(doctor.getDoctorCode());
        res.setFullName(doctor.getFullName());
        res.setAvatarUrl(doctor.getAvatarUrl());
        res.setQualification(doctor.getQualification());
        res.setSpecialization(doctor.getSpecialization());
        res.setDoctorType(doctor.getDoctorType());
        res.setWorkStatus(doctor.getWorkStatus());
        res.setDepartmentId(doctor.getDepartmentId() != null ? doctor.getDepartmentId().getId() : null);
        res.setDepartmentName(doctor.getDepartmentId() != null ? doctor.getDepartmentId().getName() : null);
        res.setActive(doctor.getActive());
        return res;
    }

    private static DepartmentResponse toDepartmentResponse(Department department) {
        if (department == null) {
            return null;
        }

        DepartmentResponse res = new DepartmentResponse();
        res.setId(department.getId());
        res.setCode(department.getCode());
        res.setName(department.getName());
        res.setDescription(department.getDescription());
        res.setActive(department.getActive());
        res.setCreatedAt(department.getCreatedAt());
        res.setUpdatedAt(department.getUpdatedAt());
        return res;
    }

    private static String format(Date value) {
        if (value == null) {
            return null;
        }

        return new SimpleDateFormat(DATETIME_PATTERN).format(value);
    }
}
