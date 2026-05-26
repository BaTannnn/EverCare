package com.evercare.repositories;

import com.evercare.pojo.Doctor;
import java.util.List;
import java.util.Map;

public interface DoctorRepository {

    List<Doctor> getDoctors(Map<String, String> params);

    Doctor getDoctorById(int id);

    Doctor getDoctorByUserId(Long userId);

    void addDoctor(Doctor doctor);

    void updateDoctor(Doctor doctor);

    long countDoctors(Map<String, String> params);
    long getTotalPages(Map<String, String> params);
}
