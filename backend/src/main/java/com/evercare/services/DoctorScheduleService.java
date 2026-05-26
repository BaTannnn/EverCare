package com.evercare.services;

import com.evercare.dtos.request.DoctorScheduleRequest;
import com.evercare.pojo.DoctorSchedule;

import java.util.List;
import java.util.Map;

public interface DoctorScheduleService {

    List<DoctorSchedule> getSchedules(Map<String, String> params);

    DoctorSchedule getScheduleById(int id);

    DoctorSchedule createSchedule(DoctorScheduleRequest req);

    DoctorSchedule updateSchedule(int id, DoctorScheduleRequest req);

    void softDelete(int id);

    long getTotalPages(Map<String, String> params);

}