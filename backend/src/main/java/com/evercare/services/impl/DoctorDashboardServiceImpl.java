package com.evercare.services.impl;

import com.evercare.dtos.response.DoctorDashboardSummaryResponse;
import com.evercare.enums.AppointmentStatus;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.Role;
import com.evercare.pojo.User;
import com.evercare.repositories.AppointmentRepository;
import com.evercare.repositories.DoctorRepository;
import com.evercare.services.DoctorDashboardService;
import com.evercare.services.UserService;
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
    private DoctorRepository doctorRepo;

    @Autowired
    private UserService userService;

    @Override
    public DoctorDashboardSummaryResponse getTodaySummary(String username) {
        Doctor doctor = getCurrentDoctor(username);
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

    private Doctor getCurrentDoctor(String username) {
        if (username == null || username.isBlank()) {
            throw new SecurityException("Vui lòng đăng nhập");
        }

        User user = this.userService.getUserByUsername(username);
        Doctor doctor = this.doctorRepo.getDoctorByUserId(user.getId());

        if (!hasRole(user, "DOCTOR")
                || doctor == null
                || Boolean.FALSE.equals(doctor.getActive())) {
            throw new SecurityException("Tài khoản hiện tại không phải bác sĩ đang hoạt động");
        }

        return doctor;
    }

    private boolean hasRole(User user, String expectedRole) {
        if (user == null || user.getRoleSet() == null) {
            return false;
        }

        String normalizedExpectedRole = expectedRole.toUpperCase();
        return user.getRoleSet().stream()
                .map(Role::getCode)
                .filter(code -> code != null)
                .map(code -> code.trim().toUpperCase())
                .anyMatch(code -> code.equals(normalizedExpectedRole) || code.equals("ROLE_" + normalizedExpectedRole));
    }
}
