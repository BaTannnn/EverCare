package com.evercare.controllers.api;

import com.evercare.dtos.request.TestResultRequest;
import com.evercare.dtos.response.StaffTestRequestDetailResponse;
import com.evercare.dtos.response.StaffTestRequestSummaryResponse;
import com.evercare.dtos.response.TestResultFileResponse;
import com.evercare.dtos.response.TestResultResponse;
import com.evercare.exceptions.CloudinaryUploadException;
import com.evercare.services.StaffTestResultService;
import com.evercare.services.TestResultFileService;
import java.security.Principal;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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

    @GetMapping("/test-requests")
    public ResponseEntity<?> getPendingRequests(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        try {
            List<StaffTestRequestSummaryResponse> result = this.staffTestResultService
                    .getPendingTestRequests(principal.getName());

            return ResponseEntity.ok(result);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/test-requests/{recordId}")
    public ResponseEntity<?> getRequestDetail(
            Principal principal,
            @PathVariable("recordId") Long recordId
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        try {
            StaffTestRequestDetailResponse result = this.staffTestResultService
                    .getTestRequestDetail(principal.getName(), recordId);

            return ResponseEntity.ok(result);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/test-results")
    public ResponseEntity<?> getResults(
            Principal principal,
            @RequestParam Map<String, String> params
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        try {
            List<TestResultResponse> result = this.staffTestResultService
                    .getTestResults(principal.getName(), params);

            return ResponseEntity.ok(result);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ngày không hợp lệ, định dạng đúng là yyyy-MM-dd"));
        }
    }

    @GetMapping("/test-results/{id}")
    public ResponseEntity<?> getResultDetail(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        try {
            TestResultResponse result = this.staffTestResultService
                    .getTestResultById(principal.getName(), id);

            return ResponseEntity.ok(result);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/test-results/{id}/file")
    public ResponseEntity<?> getResultFile(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        try {
            TestResultFileResponse file = this.testResultFileService.getFileForStaff(principal.getName(), id);
            return TestResultFileResponseBuilder.inlinePdf(file);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
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
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        try {
            TestResultResponse result = this.staffTestResultService
                    .createTestResult(principal.getName(), recordId, request);

            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (CloudinaryUploadException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
        }
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
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        try {
            TestResultResponse result = this.staffTestResultService
                    .updateTestResult(principal.getName(), id, request);

            return ResponseEntity.ok(result);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (CloudinaryUploadException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
        }
    }
}
