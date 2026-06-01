package com.evercare.utils;

import com.evercare.enums.DoctorWorkStatus;
import com.evercare.enums.MedicalServiceType;
import com.evercare.pojo.Department;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.MedicalService;
import com.evercare.repositories.DepartmentRepository;
import com.evercare.repositories.DoctorRepository;
import com.evercare.repositories.MedicalServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LookupSupport {
    @Autowired
    private DepartmentRepository departmentRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private MedicalServiceRepository medicalServiceRepo;

    public Department requireActiveDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException("Vui lòng chọn khoa");
        }

        Department department = this.departmentRepo.getDepartmentById(departmentId.intValue());
        if (department == null || Boolean.FALSE.equals(department.getActive())) {
            throw new IllegalArgumentException("Khoa không tồn tại hoặc đã ngưng hoạt động");
        }
        return department;
    }

    public Doctor requireActiveDoctor(Long doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("Vui lòng chọn bác sĩ");
        }

        Doctor doctor = this.doctorRepo.getDoctorById(doctorId.intValue());
        if (doctor == null || Boolean.FALSE.equals(doctor.getActive())) {
            throw new IllegalArgumentException("Bác sĩ không tồn tại hoặc đã ngưng hoạt động");
        }
        return doctor;
    }

    public Doctor requireWorkingDoctor(Long doctorId) {
        Doctor doctor = requireActiveDoctor(doctorId);
        if (DoctorWorkStatus.INACTIVE.getCode().equalsIgnoreCase(doctor.getWorkStatus())) {
            throw new IllegalStateException("Bác sĩ không còn làm việc");
        }
        return doctor;
    }

    public MedicalService requireActiveMedicalService(Long serviceId) {
        if (serviceId == null) {
            throw new IllegalArgumentException("Vui lòng chọn dịch vụ");
        }

        MedicalService service = this.medicalServiceRepo.getServiceById(serviceId.intValue());
        if (service == null) {
            throw new java.util.NoSuchElementException("Không tìm thấy dịch vụ");
        }
        if (Boolean.FALSE.equals(service.getActive())) {
            throw new IllegalStateException("Dịch vụ không khả dụng");
        }
        return service;
    }

    public MedicalService requireActiveExaminationService(Long serviceId) {
        MedicalService service = requireActiveMedicalService(serviceId);
        if (!MedicalServiceType.EXAMINATION.getCode().equalsIgnoreCase(service.getServiceType())) {
            throw new IllegalArgumentException("Dịch vụ được chọn không phải dịch vụ khám. Vui lòng chọn dịch vụ có loại EXAMINATION.");
        }
        return service;
    }
}
