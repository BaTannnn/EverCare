/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.controllers.api;

import com.evercare.dtos.response.MedicalServiceResponse;
import com.evercare.dtos.response.DepartmentResponse;
import com.evercare.mappers.DepartmentMapper;
import com.evercare.mappers.MedicalServiceMapper;
import com.evercare.pojo.Department;
import com.evercare.services.DepartmentService;
import com.evercare.services.MedicalServiceService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author cadic
 */
@RestController
@RequestMapping("/api/departments")
public class ApiDepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private MedicalServiceService medicalServiceService;
    
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> list(@RequestParam Map<String, String> params) {
        List<DepartmentResponse> result = this.departmentService.getDepartments(params).stream().map(DepartmentMapper::toResponse).toList();

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> retrieve(@PathVariable(value = "id") int id) {
        Department department = this.departmentService.getDepartmentById(id);

        if (department == null || Boolean.FALSE.equals(department.getActive())) {
            throw new NoSuchElementException("Không tìm thấy khoa");
        }
        return new ResponseEntity<>(DepartmentMapper.toResponse(department), HttpStatus.OK);
    }

    @GetMapping("/{departmentId}/medical-services")
    public ResponseEntity<List<MedicalServiceResponse>> listMedicalServices(@PathVariable("departmentId") int departmentId) {
        List<MedicalServiceResponse> result = this.medicalServiceService
                .getActiveExaminationServicesByDepartmentId((long) departmentId)
                .stream()
                .map(MedicalServiceMapper::toResponse)
                .toList();

        return ResponseEntity.ok(result);
    }
}
