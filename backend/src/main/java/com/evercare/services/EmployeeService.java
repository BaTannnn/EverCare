package com.evercare.services;

import com.evercare.dtos.request.EmployeeRequest;
import com.evercare.pojo.Employee;
import com.evercare.pojo.User;

import java.util.List;
import java.util.Map;

public interface EmployeeService {
    List<Employee> getEmployees(Map<String, String> params);
    Employee getEmployeeById(int id);
    Employee createEmployee(EmployeeRequest req);
    Employee updateEmployee(int id, EmployeeRequest req);
    void softDelete(int id);
    long getTotalPages(Map<String, String> params);
    List<User> getSelectableUsers(Long currentUserId);
}
