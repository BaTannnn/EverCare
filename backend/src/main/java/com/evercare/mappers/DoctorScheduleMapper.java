package com.evercare.mappers;

import com.evercare.dtos.request.DoctorScheduleRequest;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.DoctorSchedule;

public class DoctorScheduleMapper {

    public static DoctorSchedule toEntityForCreate(DoctorScheduleRequest req, Doctor doctor) {
        DoctorSchedule s = new DoctorSchedule();

        s.setDoctorId(doctor);
        s.setWorkDate(req.getWorkDate());
        s.setStartTime(req.getStartTime());
        s.setEndTime(req.getEndTime());
        s.setMaxPatients(req.getMaxPatients());
        s.setStatus(req.getStatus());
        s.setNote(req.getNote());
        s.setActive(true);

        return s;
    }

    public static void updateEntity(DoctorSchedule existing,
                                    DoctorScheduleRequest req,
                                    Doctor doctor) {
        existing.setDoctorId(doctor);
        existing.setWorkDate(req.getWorkDate());
        existing.setStartTime(req.getStartTime());
        existing.setEndTime(req.getEndTime());
        existing.setMaxPatients(req.getMaxPatients());
        existing.setStatus(req.getStatus());
        existing.setNote(req.getNote());
    }
}