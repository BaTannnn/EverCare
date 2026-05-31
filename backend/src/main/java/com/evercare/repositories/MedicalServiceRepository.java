package com.evercare.repositories;

import com.evercare.pojo.MedicalService;
import java.util.List;
import java.util.Map;

public interface MedicalServiceRepository {

    List<MedicalService> getServices(Map<String, String> params);

    MedicalService getServiceById(int id);

    List<MedicalService> getActiveExaminationServicesByDepartmentId(Long departmentId);

    void addService(MedicalService service);

    void updateService(MedicalService service);

    long countMedicalServices(Map<String, String> params);

    long getTotalPages(Map<String, String> params);
}
