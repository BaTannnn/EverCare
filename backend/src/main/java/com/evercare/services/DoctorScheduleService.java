package com.evercare.services;

import com.evercare.dtos.request.DoctorScheduleRequest;
import com.evercare.dtos.response.DoctorScheduleResponse;
import com.evercare.pojo.DoctorSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DoctorScheduleService {

    List<DoctorSchedule> getSchedules(Map<String, String> params);
    List<DoctorScheduleResponse> listAvailableSchedulesByDoctorId(Long doctorId, LocalDate from, LocalDate to);

    List<DoctorScheduleResponse> getCurrentDoctorSchedules(String username, Map<String, String> params);

    DoctorSchedule getScheduleById(int id);

    DoctorSchedule createSchedule(DoctorScheduleRequest req);

    DoctorSchedule updateSchedule(int id, DoctorScheduleRequest req);

    void softDelete(int id);

    long getTotalPages(Map<String, String> params);

}
