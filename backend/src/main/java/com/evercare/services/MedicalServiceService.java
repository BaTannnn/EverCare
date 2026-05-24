package com.evercare.services;

import com.evercare.dtos.request.MedicalServiceRequest;
import com.evercare.pojo.MedicalService;

import java.util.List;
import java.util.Map;

public interface MedicalServiceService {

    List<MedicalService> getServices(Map<String, String> params);

    MedicalService getServiceById(int id);

    MedicalService createService(MedicalServiceRequest req);

    MedicalService updateService(int id, MedicalServiceRequest req);

    void softDelete(int id);
}