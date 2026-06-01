package com.evercare.controllers.api;

import com.evercare.dtos.response.TestResultFileResponse;
import com.evercare.services.TestResultFileService;
import com.evercare.utils.PdfInlineResponseHelper;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctor/test-results")
@CrossOrigin
public class ApiDoctorTestResultFileController {
    @Autowired
    private TestResultFileService testResultFileService;

    @GetMapping("/{id}/file")
    public ResponseEntity<?> file(Principal principal, @PathVariable("id") Long id) {
        TestResultFileResponse file = this.testResultFileService.getFileForDoctor(requireUsername(principal), id);
        return PdfInlineResponseHelper.inlinePdf(file);
    }

    private String requireUsername(Principal principal) {
        if (principal == null) {
            throw new com.evercare.exceptions.AuthenticationRequiredException("Vui lòng đăng nhập");
        }
        return principal.getName();
    }
}
