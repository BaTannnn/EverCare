/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.services;

import com.evercare.dtos.request.DepartmentRequest;
import com.evercare.pojo.Department;
import java.util.List;
import java.util.Map;

/**
 *
 * @author cadic
 */
public interface DepartmentService {
    List<Department> getDepartments(Map<String, String> params);

    Department getDepartmentById(int id);

    Department createDepartment(DepartmentRequest req);

    Department updateDepartment(int id, DepartmentRequest req);

    void softDelete(int id);

    long getTotalPages(Map<String, String> params);
}
