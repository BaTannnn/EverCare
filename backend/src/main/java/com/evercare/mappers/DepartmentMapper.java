package com.evercare.mappers;

import com.evercare.dtos.request.DepartmentRequest;
import com.evercare.dtos.response.DepartmentResponse;
import com.evercare.pojo.Department;

public class DepartmentMapper {

    public static DepartmentResponse toResponse(Department d) {
        if (d == null) {
            return null;
        }

        DepartmentResponse res = new DepartmentResponse();

        res.setId(d.getId());
        res.setCode(d.getCode());
        res.setName(d.getName());
        res.setDescription(d.getDescription());
        res.setActive(d.getActive());
        res.setCreatedAt(d.getCreatedAt());
        res.setUpdatedAt(d.getUpdatedAt());

        return res;
    }

    public static Department toEntityForCreate(DepartmentRequest req) {
        Department d = new Department();

        d.setName(req.getName());
        d.setDescription(req.getDescription());
        d.setActive(true);

        return d;
    }

    public static void updateEntity(Department existing, DepartmentRequest req) {
        existing.setName(req.getName());
        existing.setDescription(req.getDescription());
    }
}