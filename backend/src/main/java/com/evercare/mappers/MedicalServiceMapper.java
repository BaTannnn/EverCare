package com.evercare.mappers;

import com.evercare.dtos.request.MedicalServiceRequest;
import com.evercare.dtos.response.MedicalServiceResponse;
import com.evercare.pojo.Department;
import com.evercare.pojo.MedicalService;

public class MedicalServiceMapper {

    public static MedicalServiceResponse toResponse(MedicalService s) {
        if (s == null) {
            return null;
        }

        MedicalServiceResponse res = new MedicalServiceResponse();

        res.setId(s.getId());
        res.setCode(s.getCode());
        res.setName(s.getName());
        res.setDescription(s.getDescription());
        res.setPrice(s.getPrice());
        res.setServiceType(s.getServiceType());
        res.setActive(s.getActive());
        res.setCreatedAt(s.getCreatedAt());
        res.setUpdatedAt(s.getUpdatedAt());

        if (s.getDepartmentId() != null) {
            res.setDepartmentId(s.getDepartmentId().getId());
            res.setDepartmentName(s.getDepartmentId().getName());
        }

        return res;
    }

    public static MedicalService toEntityForCreate(
            MedicalServiceRequest req,
            Department department
    ) {
        MedicalService s = new MedicalService();

        s.setName(req.getName());
        s.setDescription(req.getDescription());
        s.setPrice(req.getPrice());
        s.setServiceType(req.getServiceType());
        s.setDepartmentId(department);
        s.setActive(true);

        return s;
    }

    public static void updateEntity(
            MedicalService existing,
            MedicalServiceRequest req,
            Department department
    ) {
        existing.setName(req.getName());
        existing.setDescription(req.getDescription());
        existing.setPrice(req.getPrice());
        existing.setServiceType(req.getServiceType());
        existing.setDepartmentId(department);
    }
}