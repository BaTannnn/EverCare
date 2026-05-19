/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.services;

import com.evercare.pojo.Department;
import java.util.List;
import java.util.Map;

/**
 *
 * @author cadic
 */
public interface DepartmentService {
    List<Department> getDeparments(Map<String, String> params);
    Department getDeparmentById(int id);
    void addOrUpdateDepartment(Department d);
    void sotfDelete(int id);
}
