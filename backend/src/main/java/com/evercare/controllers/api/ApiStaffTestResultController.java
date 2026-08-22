package com.evercare.controllers.api;

import com.evercare.dtos.request.TestResultRequest;
import com.evercare.dtos.response.StaffTestRequestDetailResponse;
import com.evercare.dtos.response.StaffTestRequestSummaryResponse;
import com.evercare.dtos.response.TestResultFileResponse;
import com.evercare.dtos.response.TestResultResponse;
import com.evercare.services.StaffTestResultService;
import com.evercare.services.TestResultFileService;
import com.evercare.utils.AuthSupport;
import com.evercare.utils.PdfInlineResponseHelper;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin
public class ApiStaffTestResultController {
    @Autowired
    private StaffTestResultService staffTestResultService;
    @Autowired
    private TestResultFileService testResultFileService;
    @Autowired
    private AuthSupport authSupport;
    @GetMapping("/test-requests")
    public ResponseEntity<?> getPendingRequests(
            Principal principal,
            @RequestParam Map<String, String> params
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "LAB_TECH", "Tài khoản hiện tại không phải nhân viên xét nghiệm đang hoạt động");
        List<StaffTestRequestSummaryResponse> result = this.staffTestResultService
                .getPendingTestRequests(username, params);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/test-requests/{recordId}")
    public ResponseEntity<?> getRequestDetail(
            Principal principal,
            @PathVariable("recordId") Long recordId
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "LAB_TECH", "Tài khoản hiện tại không phải nhân viên xét nghiệm đang hoạt động");
        StaffTestRequestDetailResponse result = this.staffTestResultService
                .getTestRequestDetail(username, recordId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/test-results")
    public ResponseEntity<?> getResults(
            Principal principal,
            @RequestParam Map<String, String> params
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "LAB_TECH", "Tài khoản hiện tại không phải nhân viên xét nghiệm đang hoạt động");
        List<TestResultResponse> result = this.staffTestResultService
                .getTestResults(username, params);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/test-results/{id}")
    public ResponseEntity<?> getResultDetail(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "LAB_TECH", "Tài khoản hiện tại không phải nhân viên xét nghiệm đang hoạt động");
        TestResultResponse result = this.staffTestResultService
                .getTestResultById(username, id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/test-results/{id}/file")
    public ResponseEntity<?> getResultFile(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "LAB_TECH", "Tài khoản hiện tại không phải nhân viên xét nghiệm đang hoạt động");
        TestResultFileResponse file = this.testResultFileService.getFileForStaff(username, id);
        return PdfInlineResponseHelper.inlinePdf(file);
    }

    @PostMapping(
            path = "/medical-records/{recordId}/test-results",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> create(
            Principal principal,
            @PathVariable("recordId") Long recordId,
            @RequestBody TestResultRequest request
    ) {
        return createInternal(principal, recordId, request);
    }

    @PostMapping(
            path = "/medical-records/{recordId}/test-results",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> createWithFile(
            Principal principal,
            @PathVariable("recordId") Long recordId,
            @ModelAttribute TestResultRequest request
    ) {
        return createInternal(principal, recordId, request);
    }

    private ResponseEntity<?> createInternal(
            Principal principal,
            Long recordId,
            TestResultRequest request
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "LAB_TECH", "Tài khoản hiện tại không phải nhân viên xét nghiệm đang hoạt động");
        TestResultResponse result = this.staffTestResultService
                .createTestResult(username, recordId, request);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping(
            path = "/test-results/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> update(
            Principal principal,
            @PathVariable("id") Long id,
            @RequestBody(required = false) TestResultRequest request
    ) {
        return updateInternal(principal, id, request);
    }

    @PutMapping(
            path = "/test-results/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> updateWithFile(
            Principal principal,
            @PathVariable("id") Long id,
            @ModelAttribute TestResultRequest request
    ) {
        return updateInternal(principal, id, request);
    }

    private ResponseEntity<?> updateInternal(
            Principal principal,
            Long id,
            TestResultRequest request
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "LAB_TECH", "Tài khoản hiện tại không phải nhân viên xét nghiệm đang hoạt động");
        TestResultResponse result = this.staffTestResultService
                .updateTestResult(username, id, request);
        return ResponseEntity.ok(result);
    }
}
