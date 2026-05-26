/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.services.impl;

import com.evercare.dtos.request.DepartmentRequest;
import com.evercare.mappers.DepartmentMapper;
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
    public List<Department> getDepartments(Map<String, String> params) {
        return this.departmentRepo.getDepartments(params);
    }

    @Override
    public Department getDepartmentById(int id) {
        return this.departmentRepo.getDepartmentById(id);
    }

    @Override
    public Department createDepartment(DepartmentRequest req) {
        validateDepartment(req);

        Department d = DepartmentMapper.toEntityForCreate(req);

        this.departmentRepo.addDepartment(d);

        return d;
    }

    @Override
    public Department updateDepartment(int id, DepartmentRequest req) {
        validateDepartment(req);

        Department existing = this.departmentRepo.getDepartmentById(id);

        if (existing == null) {
            throw new IllegalArgumentException("Khoa không tồn tại");
        }

        if (Boolean.FALSE.equals(existing.getActive())) {
            throw new IllegalArgumentException("Khoa đã bị xóa hoặc ngưng hoạt động");
        }

        DepartmentMapper.updateEntity(existing, req);

        this.departmentRepo.updateDepartment(existing);

        return existing;
    }

    @Override
    public void softDelete(int id) {
        Department existing = this.departmentRepo.getDepartmentById(id);

        if (existing == null) {
            throw new IllegalArgumentException("Khoa không tồn tại");
        }

        existing.setActive(false);

        this.departmentRepo.updateDepartment(existing);
    }

    @Override
    public long getTotalPages(Map<String, String> params) {
        return this.departmentRepo.getTotalPages(params);
    }

    private void validateDepartment(DepartmentRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Tên khoa không được để trống");
        }

        if (req.getName().trim().length() < 2) {
            throw new IllegalArgumentException("Tên khoa phải có ít nhất 2 ký tự");
        }

        req.setName(req.getName().trim());

        if (req.getDescription() != null) {
            req.setDescription(req.getDescription().trim());
        }
    }
}
