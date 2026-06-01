package com.evercare.services.impl;

import com.evercare.dtos.request.MedicalServiceRequest;
import com.evercare.mappers.MedicalServiceMapper;
import com.evercare.pojo.Department;
import com.evercare.pojo.MedicalService;
import com.evercare.repositories.DepartmentRepository;
import com.evercare.repositories.MedicalServiceRepository;
import com.evercare.services.MedicalServiceService;
import com.evercare.utils.LookupSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Transactional
public class MedicalServiceServiceImpl implements MedicalServiceService {

    @Autowired
    private MedicalServiceRepository serviceRepo;

    @Autowired
    private DepartmentRepository departmentRepo;

    @Autowired
    private LookupSupport lookupSupport;

    @Override
    public List<MedicalService> getServices(Map<String, String> params) {
        return this.serviceRepo.getServices(params);
    }

    @Override
    public MedicalService getServiceById(int id) {
        return this.serviceRepo.getServiceById(id);
    }

    @Override
    public List<MedicalService> getActiveExaminationServicesByDepartmentId(Long departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException("Vui lòng chọn khoa");
        }

        Department department = this.departmentRepo.getDepartmentById(departmentId.intValue());
        if (department == null || Boolean.FALSE.equals(department.getActive())) {
            throw new NoSuchElementException("Không tìm thấy khoa");
        }

        return this.serviceRepo.getActiveExaminationServicesByDepartmentId(departmentId);
    }

    @Override
    public MedicalService createService(MedicalServiceRequest req) {
        validateService(req);

        Department department = this.lookupSupport.requireActiveDepartment(req.getDepartmentId());

        MedicalService service = MedicalServiceMapper.toEntityForCreate(req, department);

        this.serviceRepo.addService(service);

        return service;
    }

    @Override
    public MedicalService updateService(int id, MedicalServiceRequest req) {
        validateService(req);

        MedicalService existing = this.serviceRepo.getServiceById(id);

        if (existing == null) {
            throw new IllegalArgumentException("Dịch vụ không tồn tại");
        }

        if (Boolean.FALSE.equals(existing.getActive())) {
            throw new IllegalArgumentException("Dịch vụ đã bị xóa hoặc ngưng hoạt động");
        }

        Department department = this.lookupSupport.requireActiveDepartment(req.getDepartmentId());

        MedicalServiceMapper.updateEntity(existing, req, department);

        this.serviceRepo.updateService(existing);

        return existing;
    }

    @Override
    public void softDelete(int id) {
        MedicalService existing = this.serviceRepo.getServiceById(id);

        if (existing == null) {
            throw new IllegalArgumentException("Dịch vụ không tồn tại");
        }

        existing.setActive(false);

        this.serviceRepo.updateService(existing);
    }

    @Override
    public long getTotalPages(Map<String, String> params) {
        return this.serviceRepo.getTotalPages(params);
    }

    private void validateService(MedicalServiceRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống");
        }

        if (req.getName().trim().length() < 2) {
            throw new IllegalArgumentException("Tên dịch vụ phải có ít nhất 2 ký tự");
        }

        req.setName(req.getName().trim());

        if (req.getDescription() != null) {
            req.setDescription(req.getDescription().trim());
        }

        if (req.getPrice() == null) {
            req.setPrice(BigDecimal.ZERO);
        }

        if (req.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá dịch vụ không được âm");
        }

        if (req.getServiceType() == null || req.getServiceType().isBlank()) {
            req.setServiceType("CONSULTATION");
        } else {
            req.setServiceType(req.getServiceType().trim().toUpperCase());
        }
    }
}
