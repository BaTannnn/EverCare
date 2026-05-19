/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.services.impl;

import com.evercare.pojo.Department;
import com.evercare.repositories.DepartmentRepository;
import com.evercare.services.DepartmentService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author cadic
 */
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepo;

    @Override
    public List<Department> getDeparments(Map<String, String> params) {
        return this.departmentRepo.getDeparments(params);
    }

    @Override
    public Department getDeparmentById(int id) {
        return this.departmentRepo.getDeparmentById(id);
    }

    @Override
    public void addOrUpdateDepartment(Department d) {
        validateDepartment(d);
        if (d.getId() == null) {
//            d.setCode("DEP" + System.currentTimeMillis());
            d.setCreatedAt(new java.util.Date());
            d.setUpdatedAt(new java.util.Date());
            d.setActive(true);
        } else {
            Department existingDep = this.departmentRepo.getDeparmentById(d.getId().intValue());
            d.setCode(existingDep.getCode());
            d.setCreatedAt(existingDep.getCreatedAt());
            d.setActive(existingDep.getActive());

            d.setUpdatedAt(new java.util.Date());
        }
        this.departmentRepo.addOrUpdateDepartment(d);
    }

    @Override
    public void sotfDelete(int id) {
        this.departmentRepo.sotfDelete(id);
    }
    
    private void validateDepartment(Department department) {
        if (department.getName() == null || department.getName().isBlank()) {
            throw new IllegalArgumentException("Tên khoa không được để trống");
        }

        if (department.getName().trim().length() < 2) {
            throw new IllegalArgumentException("Tên khoa phải có ít nhất 2 ký tự");
        }

        department.setName(department.getName().trim());

        if (department.getDescription() != null) {
            department.setDescription(department.getDescription().trim());
        }
    }
}
