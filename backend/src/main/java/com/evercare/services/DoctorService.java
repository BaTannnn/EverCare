package com.evercare.services;

import com.evercare.dtos.request.DoctorRequest;
import com.evercare.pojo.Doctor;

import java.util.List;
import java.util.Map;

public interface DoctorService {

    List<Doctor> getDoctors(Map<String, String> params);

    Doctor getDoctorById(int id);

    Doctor createDoctor(DoctorRequest req);

    Doctor updateDoctor(int id, DoctorRequest req);

    void softDelete(int id);

    long getTotalPages(Map<String, String> params);
}