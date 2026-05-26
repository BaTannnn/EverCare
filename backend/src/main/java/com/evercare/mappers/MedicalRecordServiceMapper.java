package com.evercare.mappers;

import com.evercare.dtos.response.MedicalRecordServiceResponse;
import com.evercare.dtos.response.TestResultResponse;
import com.evercare.pojo.MedicalRecordService;
import com.evercare.pojo.MedicalService;
import com.evercare.pojo.TestResult;
import java.util.Collections;
import java.util.List;

public final class MedicalRecordServiceMapper {
    private MedicalRecordServiceMapper() {
    }

    public static MedicalRecordServiceResponse toResponse(
            MedicalRecordService recordService,
            List<TestResult> testResults
    ) {
        MedicalRecordServiceResponse res = new MedicalRecordServiceResponse();
        MedicalService service = recordService.getServiceId();

        res.setId(recordService.getId());
        res.setMedicalRecordId(recordService.getMedicalRecordId() != null ? recordService.getMedicalRecordId().getId() : null);
        res.setServiceId(service != null ? service.getId() : null);
        res.setServiceCode(service != null ? service.getCode() : null);
        res.setServiceName(service != null ? service.getName() : null);
        res.setServiceType(service != null ? service.getServiceType() : null);
        res.setQuantity(recordService.getQuantity());
        res.setUnitPrice(recordService.getUnitPrice());
        res.setResultSummary(recordService.getResultSummary());

        List<TestResultResponse> resultResponses = testResults == null
                ? Collections.emptyList()
                : testResults.stream().map(TestResultMapper::toResponse).toList();
        res.setTestResults(resultResponses);

        return res;
    }
}
