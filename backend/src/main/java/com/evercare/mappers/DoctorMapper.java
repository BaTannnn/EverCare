package com.evercare.mappers;

import com.evercare.dtos.request.DoctorRequest;
import com.evercare.pojo.Department;
import com.evercare.pojo.Doctor;

public class DoctorMapper {

    public static Doctor toEntityForCreate(DoctorRequest req, Department department) {
        Doctor d = new Doctor();

        d.setDepartmentId(department);
        d.setFullName(req.getFullName());
        d.setPhone(req.getPhone());
        d.setEmail(req.getEmail());
        d.setQualification(req.getQualification());
        d.setSpecialization(req.getSpecialization());
        d.setDoctorType(req.getDoctorType());
        d.setWorkStatus(req.getWorkStatus());
        d.setBaseSalary(req.getBaseSalary());
        d.setHourlyRate(req.getHourlyRate());
        d.setBio(req.getBio());
        d.setActive(true);

        return d;
    }

    public static void updateEntity(Doctor existing, DoctorRequest req, Department department) {
        existing.setDepartmentId(department);
        existing.setFullName(req.getFullName());
        existing.setPhone(req.getPhone());
        existing.setEmail(req.getEmail());
        existing.setQualification(req.getQualification());
        existing.setSpecialization(req.getSpecialization());
        existing.setDoctorType(req.getDoctorType());
        existing.setWorkStatus(req.getWorkStatus());
        existing.setBaseSalary(req.getBaseSalary());
        existing.setHourlyRate(req.getHourlyRate());
        existing.setBio(req.getBio());
    }
}