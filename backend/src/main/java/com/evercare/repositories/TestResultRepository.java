package com.evercare.repositories;

import com.evercare.pojo.TestResult;
import java.util.List;

public interface TestResultRepository {
    void addTestResult(TestResult testResult);

    TestResult getTestResultById(Long id);

    void updateTestResult(TestResult testResult);

    List<TestResult> getTestResultsByMedicalRecordId(Long recordId);
}
