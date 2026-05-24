/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.repositories;

import com.evercare.pojo.Department;
import java.util.List;
import java.util.Map;

/**
 *
 * @author cadic
 */
public interface DepartmentRepository {
    List<Department> getDepartments(Map<String, String> params);
    Department getDepartmentById(int id);
    void addDepartment(Department department);
    void updateDepartment(Department department);
    void softDelete(int id);
}
