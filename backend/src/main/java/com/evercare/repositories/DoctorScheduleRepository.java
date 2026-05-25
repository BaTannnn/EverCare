package com.evercare.repositories;

import com.evercare.pojo.DoctorSchedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface DoctorScheduleRepository {

    List<DoctorSchedule> getSchedules(Map<String, String> params);

    DoctorSchedule getScheduleById(int id);

    void addSchedule(DoctorSchedule schedule);

    void updateSchedule(DoctorSchedule schedule);

    boolean existsOverlappingSchedule(Long doctorId,
                                      LocalDate workDate,
                                      LocalTime startTime,
                                      LocalTime endTime,
                                      Long excludeId);
}