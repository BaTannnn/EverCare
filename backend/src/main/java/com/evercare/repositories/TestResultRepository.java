package com.evercare.repositories;

import com.evercare.pojo.TestResult;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TestResultRepository {
    void addTestResult(TestResult testResult);

    TestResult getTestResultById(Long id);

    void updateTestResult(TestResult testResult);

    List<TestResult> getTestResultsByMedicalRecordId(Long recordId);
    List<TestResult> getTestResultsByPatientId(Long patientId, LocalDate from, LocalDate to, Map<String, String> params);
}
