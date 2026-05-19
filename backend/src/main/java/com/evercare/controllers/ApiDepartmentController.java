/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.controllers;

import com.evercare.pojo.Department;
import com.evercare.services.DepartmentService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author cadic
 */
@RestController
@RequestMapping("/api")
public class ApiDepartmentController {
    @Autowired
    private DepartmentService departmentService;
    
    @GetMapping("/departments")
    public ResponseEntity<List<Department>> list(@RequestParam Map<String, String> params) {
        List<Department> result = this.departmentService.getDeparments(params);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @GetMapping("/departments/{departmentId}")
    public ResponseEntity<Department> retrieve(@PathVariable(value = "departmentId") int id) {
        Department result = this.departmentService.getDeparmentById(id);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
