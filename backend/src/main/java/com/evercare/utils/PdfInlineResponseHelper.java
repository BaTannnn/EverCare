package com.evercare.utils;

import com.evercare.dtos.response.TestResultFileResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class PdfInlineResponseHelper {
    private PdfInlineResponseHelper() {
    }

    public static ResponseEntity<byte[]> inlinePdf(TestResultFileResponse file) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(file.getFilename())
                        .build()
                        .toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file.getContent());
    }
}
