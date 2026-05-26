package com.evercare.repositories;

import com.evercare.pojo.Employee;

public interface EmployeeRepository {
    Employee getEmployeeByUserId(Long userId);
}
