package com.evercare.services;

import com.evercare.dtos.response.TestResultFileResponse;

public interface TestResultFileService {
    TestResultFileResponse getFileForDoctor(String username, Long resultId);

    TestResultFileResponse getFileForStaff(String username, Long resultId);

    TestResultFileResponse getFileForPatient(String username, Long resultId);
}
