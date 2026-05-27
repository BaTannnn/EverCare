/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.controllers.api;

import com.evercare.dtos.request.DepartmentRequest;
import com.evercare.dtos.response.DepartmentResponse;
import com.evercare.mappers.DepartmentMapper;
import com.evercare.pojo.Department;
import com.evercare.services.DepartmentService;
import java.util.List;
import java.util.Map;
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
    
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> list(@RequestParam Map<String, String> params) {
        List<DepartmentResponse> result = this.departmentService.getDepartments(params).stream().map(DepartmentMapper::toResponse).toList();

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> retrieve(@PathVariable(value = "id") int id) {
        Department department = this.departmentService.getDepartmentById(id);

        if (department == null || Boolean.FALSE.equals(department.getActive())) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(DepartmentMapper.toResponse(department), HttpStatus.OK);
    }

//    @PostMapping
//    public ResponseEntity<?> create(@RequestBody DepartmentRequest req) {
//        try {
//            Department created = this.departmentService.createDepartment(req);
//
//            return new ResponseEntity<>(DepartmentMapper.toResponse(created), HttpStatus.CREATED);
//
//        } catch (IllegalArgumentException ex) {
//            return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<?> update(
//            @PathVariable int id,
//            @RequestBody DepartmentRequest req
//    ) {
//        try {
//            Department updated = this.departmentService.updateDepartment(id, req);
//
//            return ResponseEntity.ok(DepartmentMapper.toResponse(updated));
//
//        } catch (IllegalArgumentException ex) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(Map.of("message", ex.getMessage()));
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> destroy(@PathVariable int id) {
//        try {
//            this.departmentService.softDelete(id);
//
//            return ResponseEntity.noContent().build();
//
//        } catch (IllegalArgumentException ex) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(Map.of("message", ex.getMessage()));
//        }
//    }
}
