package com.evercare.services.impl;

import com.evercare.dtos.response.DoctorAppointmentResponse;
import com.evercare.mappers.AppointmentMapper;
import com.evercare.pojo.Appointment;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.User;
import com.evercare.repositories.AppointmentRepository;
import com.evercare.repositories.DoctorRepository;
import com.evercare.services.DoctorAppointmentService;
import com.evercare.services.UserService;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorAppointmentServiceImpl implements DoctorAppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private UserService userService;

    @Override
    public List<DoctorAppointmentResponse> getAppointmentsByDate(String username, LocalDate date) {
        Doctor doctor = getCurrentDoctor(username);

        return this.appointmentRepo
                .getAppointmentsByDoctorAndDate(doctor.getId(), Date.valueOf(date))
                .stream()
                .map(AppointmentMapper::toDoctorResponse)
                .toList();
    }

    @Override
    public DoctorAppointmentResponse getAppointmentById(String username, Long appointmentId) {
        Doctor doctor = getCurrentDoctor(username);
        Appointment appointment = this.appointmentRepo.getAppointmentByDoctorAndId(doctor.getId(), appointmentId);

        if (appointment == null) {
            throw new NoSuchElementException("Không tìm thấy lịch hẹn");
        }

        return AppointmentMapper.toDoctorResponse(appointment);
    }

    private Doctor getCurrentDoctor(String username) {
        if (username == null || username.isBlank()) {
            throw new SecurityException("Vui lòng đăng nhập");
        }

        User user = this.userService.getUserByUsername(username);
        Doctor doctor = this.doctorRepo.getDoctorByUserId(user.getId());

        if (doctor == null || Boolean.FALSE.equals(doctor.getActive())) {
            throw new SecurityException("Tài khoản hiện tại không phải bác sĩ đang hoạt động");
        }

        return doctor;
    }
}
