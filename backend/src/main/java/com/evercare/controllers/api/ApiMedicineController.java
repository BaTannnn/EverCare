package com.evercare.controllers.api;

import com.evercare.dtos.response.MedicineResponse;
import com.evercare.services.MedicineService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medicines")
@CrossOrigin
public class ApiMedicineController {
    @Autowired
    private MedicineService medicineService;

    @GetMapping
    public ResponseEntity<List<MedicineResponse>> list(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(this.medicineService.getMedicines(params));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> retrieve(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(this.medicineService.getMedicineById(id));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }
}
