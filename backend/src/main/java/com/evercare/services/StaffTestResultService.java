package com.evercare.services;

import com.evercare.dtos.request.TestResultRequest;
import com.evercare.dtos.response.StaffTestRequestDetailResponse;
import com.evercare.dtos.response.StaffTestRequestSummaryResponse;
import com.evercare.dtos.response.TestResultResponse;
import java.util.List;
import java.util.Map;

public interface StaffTestResultService {
    List<StaffTestRequestSummaryResponse> getPendingTestRequests(String username);

    StaffTestRequestDetailResponse getTestRequestDetail(String username, Long recordId);

    List<TestResultResponse> getTestResults(String username, Map<String, String> params);

    TestResultResponse getTestResultById(String username, Long id);

    TestResultResponse createTestResult(String username, Long recordId, TestResultRequest request);

    TestResultResponse updateTestResult(String username, Long id, TestResultRequest request);
}
