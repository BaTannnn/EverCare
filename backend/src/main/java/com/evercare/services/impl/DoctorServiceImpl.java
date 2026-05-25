package com.evercare.services.impl;

import com.evercare.dtos.request.DoctorRequest;
import com.evercare.enums.DoctorType;
import com.evercare.enums.DoctorWorkStatus;
import com.evercare.mappers.DoctorMapper;
import com.evercare.pojo.Department;
import com.evercare.pojo.Doctor;
import com.evercare.repositories.DepartmentRepository;
import com.evercare.repositories.DoctorRepository;
import com.evercare.services.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private DepartmentRepository departmentRepo;

    @Override
    public List<Doctor> getDoctors(Map<String, String> params) {
        return this.doctorRepo.getDoctors(params);
    }

    @Override
    public Doctor getDoctorById(int id) {
        return this.doctorRepo.getDoctorById(id);
    }

    @Override
    public Doctor createDoctor(DoctorRequest req) {
        validateDoctor(req);

        Department department = loadValidDepartment(req.getDepartmentId());

        Doctor doctor = DoctorMapper.toEntityForCreate(req, department);

        this.doctorRepo.addDoctor(doctor);

        return doctor;
    }

    @Override
    public Doctor updateDoctor(int id, DoctorRequest req) {
        validateDoctor(req);

        Doctor existing = this.doctorRepo.getDoctorById(id);

        if (existing == null) {
            throw new IllegalArgumentException("Bác sĩ không tồn tại");
        }

        if (Boolean.FALSE.equals(existing.getActive())) {
            throw new IllegalArgumentException("Bác sĩ đã bị xóa hoặc ngưng hoạt động");
        }

        Department department = loadValidDepartment(req.getDepartmentId());

        DoctorMapper.updateEntity(existing, req, department);

        this.doctorRepo.updateDoctor(existing);

        return existing;
    }

    @Override
    public void softDelete(int id) {
        Doctor existing = this.doctorRepo.getDoctorById(id);

        if (existing == null) {
            throw new IllegalArgumentException("Bác sĩ không tồn tại");
        }

        existing.setActive(false);
        existing.setWorkStatus(DoctorWorkStatus.INACTIVE.getCode());

        this.doctorRepo.updateDoctor(existing);
    }

    private Department loadValidDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException("Vui lòng chọn khoa");
        }

        Department department = this.departmentRepo.getDepartmentById(departmentId.intValue());

        if (department == null || Boolean.FALSE.equals(department.getActive())) {
            throw new IllegalArgumentException("Khoa không tồn tại hoặc đã ngưng hoạt động");
        }

        return department;
    }

    private void validateDoctor(DoctorRequest req) {
        if (req.getFullName() == null || req.getFullName().isBlank()) {
            throw new IllegalArgumentException("Họ tên bác sĩ không được để trống");
        }

        if (req.getFullName().trim().length() < 2) {
            throw new IllegalArgumentException("Họ tên bác sĩ phải có ít nhất 2 ký tự");
        }

        req.setFullName(req.getFullName().trim());

        if (req.getPhone() != null) {
            req.setPhone(req.getPhone().trim());
        }

        if (req.getEmail() != null) {
            req.setEmail(req.getEmail().trim());
        }

        if (req.getAvatarUrl() != null) {
            req.setAvatarUrl(req.getAvatarUrl().trim());
        }

        if (req.getQualification() != null) {
            req.setQualification(req.getQualification().trim());
        }

        if (req.getSpecialization() != null) {
            req.setSpecialization(req.getSpecialization().trim());
        }

        if (req.getBio() != null) {
            req.setBio(req.getBio().trim());
        }

        if (req.getBaseSalary() == null) {
            req.setBaseSalary(BigDecimal.ZERO);
        }

        if (req.getHourlyRate() == null) {
            req.setHourlyRate(BigDecimal.ZERO);
        }

        if (req.getBaseSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Lương cơ bản không được âm");
        }

        if (req.getHourlyRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Lương theo giờ không được âm");
        }

        req.setDoctorType(DoctorType.normalize(req.getDoctorType()));
        req.setWorkStatus(DoctorWorkStatus.normalize(req.getWorkStatus()));
    }
}