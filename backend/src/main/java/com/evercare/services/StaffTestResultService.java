package com.evercare.services;

import com.evercare.dtos.request.TestResultRequest;
import com.evercare.dtos.response.TestResultResponse;

public interface StaffTestResultService {
    TestResultResponse createTestResult(String username, Long recordId, TestResultRequest request);

    TestResultResponse updateTestResult(String username, Long id, TestResultRequest request);
}
