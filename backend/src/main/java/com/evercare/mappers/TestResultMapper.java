package com.evercare.mappers;

import com.evercare.dtos.response.TestResultResponse;
import com.evercare.pojo.Employee;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.MedicalService;
import com.evercare.pojo.TestResult;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class TestResultMapper {
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private TestResultMapper() {
    }

    public static TestResultResponse toResponse(TestResult testResult) {
        TestResultResponse res = new TestResultResponse();
        MedicalRecord medicalRecord = testResult.getMedicalRecordId();
        MedicalService service = testResult.getServiceId();
        Employee employee = testResult.getPerformedBy();

        res.setId(testResult.getId());
        res.setResultCode(testResult.getResultCode());
        res.setResultTitle(testResult.getResultTitle());
        res.setResultContent(testResult.getResultContent());
        res.setFileUrl(testResult.getFileUrl());
        res.setConclusion(testResult.getConclusion());
        res.setResultDate(format(testResult.getResultDate()));
        res.setMedicalRecordId(medicalRecord != null ? medicalRecord.getId() : null);
        res.setServiceId(service != null ? service.getId() : null);
        res.setServiceName(service != null ? service.getName() : null);
        res.setPerformedById(employee != null ? employee.getId() : null);
        res.setPerformedByName(employee != null ? employee.getFullName() : null);

        return res;
    }

    private static String format(Date value) {
        if (value == null) {
            return null;
        }

        return new SimpleDateFormat(DATETIME_PATTERN).format(value);
    }
}
