package com.evercare.controllers.api;

import com.evercare.dtos.response.TestResultFileResponse;
import com.evercare.services.TestResultFileService;
import java.security.Principal;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patient/test-results")
@CrossOrigin
public class ApiPatientTestResultFileController {
    @Autowired
    private TestResultFileService testResultFileService;

    @GetMapping("/{id}/file")
    public ResponseEntity<?> file(Principal principal, @PathVariable("id") Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        try {
            TestResultFileResponse file = this.testResultFileService.getFileForPatient(principal.getName(), id);
            return TestResultFileResponseBuilder.inlinePdf(file);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }
}
