package com.evercare.services.impl;

import com.evercare.dtos.response.DoctorDashboardSummaryResponse;
import com.evercare.enums.AppointmentStatus;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.User;
import com.evercare.repositories.AppointmentRepository;
import com.evercare.services.DoctorDashboardService;
import com.evercare.utils.AuthSupport;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorDashboardServiceImpl implements DoctorDashboardService {
    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private AuthSupport authSupport;

    @Override
    public DoctorDashboardSummaryResponse getTodaySummary(String username) {
        Doctor doctor = this.authSupport.requireCurrentDoctor(username);
        Map<String, Long> counts = this.appointmentRepo.countAppointmentsByDoctorAndDate(
                doctor.getId(),
                Date.valueOf(LocalDate.now())
        );

        DoctorDashboardSummaryResponse res = new DoctorDashboardSummaryResponse();
        res.setTodayAppointments(counts.values().stream().mapToLong(Long::longValue).sum());
        res.setWaitingAppointments(counts.getOrDefault(AppointmentStatus.WAITING.getCode(), 0L));
        res.setInProgressAppointments(counts.getOrDefault(AppointmentStatus.IN_PROGRESS.getCode(), 0L));
        res.setCompletedAppointments(counts.getOrDefault(AppointmentStatus.COMPLETED.getCode(), 0L));
        res.setCancelledAppointments(counts.getOrDefault(AppointmentStatus.CANCELLED.getCode(), 0L));

        return res;
    }

}
