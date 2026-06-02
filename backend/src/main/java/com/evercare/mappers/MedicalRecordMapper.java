package com.evercare.mappers;

import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.dtos.response.MedicalRecordDetailResponse;
import com.evercare.dtos.response.MedicalRecordServiceResponse;
import com.evercare.dtos.response.TestResultResponse;
import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.MedicalRecordService;
import com.evercare.pojo.Prescription;
import com.evercare.pojo.TestResult;
import java.math.BigDecimal;
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
        res.setInvoice(medicalRecord.getInvoice() != null
                ? InvoiceMapper.toEmbeddedResponse(medicalRecord.getInvoice(), calculateTotalTestAmount(services))
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

    private static BigDecimal calculateTotalTestAmount(List<MedicalRecordService> services) {
        if (services == null || services.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return services.stream()
                .filter(service -> !Boolean.FALSE.equals(service.getActive()))
                .filter(MedicalRecordMapper::isTestService)
                .map(MedicalRecordMapper::calculateServiceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static boolean isTestService(MedicalRecordService recordService) {
        if (recordService.getServiceId() == null || recordService.getServiceId().getServiceType() == null) {
            return false;
        }

        String serviceType = recordService.getServiceId().getServiceType().trim();
        return "TEST".equalsIgnoreCase(serviceType) || "LAB_TEST".equalsIgnoreCase(serviceType);
    }

    private static BigDecimal calculateServiceAmount(MedicalRecordService recordService) {
        BigDecimal unitPrice = recordService.getUnitPrice() != null ? recordService.getUnitPrice() : BigDecimal.ZERO;
        int quantity = recordService.getQuantity() != null ? recordService.getQuantity() : 0;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private static String format(Date value) {
        if (value == null) {
            return null;
        }

        return new SimpleDateFormat(DATETIME_PATTERN).format(value);
    }
}
