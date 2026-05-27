package com.evercare.mappers;

import com.evercare.dtos.request.DoctorScheduleRequest;
import com.evercare.dtos.response.DoctorScheduleResponse;
import com.evercare.enums.DoctorScheduleStatus;
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

    public static DoctorScheduleResponse toResponse(DoctorSchedule schedule) {
        if (schedule == null) {
            return null;
        }

        DoctorScheduleResponse res = new DoctorScheduleResponse();
        res.setId(schedule.getId());
        res.setWorkDate(schedule.getWorkDate() != null ? schedule.getWorkDate().toString() : null);
        res.setStartTime(schedule.getStartTime() != null ? schedule.getStartTime().toString() : null);
        res.setEndTime(schedule.getEndTime() != null ? schedule.getEndTime().toString() : null);
        res.setMaxPatients(schedule.getMaxPatients());
        res.setStatus(schedule.getStatus());
        res.setStatusLabel(DoctorScheduleStatus.labelOf(schedule.getStatus()));
        res.setNote(schedule.getNote());

        Doctor doctor = schedule.getDoctorId();
        if (doctor != null) {
            res.setDoctorId(doctor.getId());
            res.setDoctorName(doctor.getFullName());

            if (doctor.getDepartmentId() != null) {
                res.setDepartmentId(doctor.getDepartmentId().getId());
                res.setDepartmentName(doctor.getDepartmentId().getName());
            }
        }

        return res;
    }
}
