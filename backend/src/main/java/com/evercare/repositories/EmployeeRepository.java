package com.evercare.repositories;

import com.evercare.pojo.Employee;
import java.util.List;
import java.util.Map;

public interface EmployeeRepository {
    List<Employee> getEmployees(Map<String, String> params);
    Employee getEmployeeById(int id);
    Employee getEmployeeByUserId(Long userId);
    void addEmployee(Employee employee);
    void updateEmployee(Employee employee);
    long countEmployees(Map<String, String> params);
    long getTotalPages(Map<String, String> params);
}
