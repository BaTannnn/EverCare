package com.evercare.services.impl;

import com.evercare.dtos.request.DoctorScheduleRequest;
import com.evercare.dtos.response.DoctorScheduleResponse;
import com.evercare.enums.DoctorScheduleStatus;
import com.evercare.mappers.DoctorScheduleMapper;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.DoctorSchedule;
import com.evercare.pojo.User;
import com.evercare.repositories.AppointmentRepository;
import com.evercare.repositories.DoctorRepository;
import com.evercare.repositories.DoctorScheduleRepository;
import com.evercare.services.DoctorScheduleService;
import com.evercare.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    @Autowired
    private DoctorScheduleRepository scheduleRepo;

    @Autowired
    private DoctorRepository doctorRepo;
    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Override
    public List<DoctorSchedule> getSchedules(Map<String, String> params) {
        return this.scheduleRepo.getSchedules(params);
    }

    @Override
    public List<DoctorScheduleResponse> listAvailableSchedulesByDoctorId(Long doctorId, LocalDate from, LocalDate to) {
        if (doctorId == null) {
            throw new IllegalArgumentException("Vui lòng chọn bác sĩ");
        }

        LocalDate startDate = from != null ? from : LocalDate.now();
        LocalDate endDate = to != null ? to : startDate.plusDays(7);

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu");
        }

        List<DoctorSchedule> schedules = this.scheduleRepo.getAvailableSchedulesByDoctorId(doctorId, startDate, endDate);
        List<DoctorScheduleResponse> result = new ArrayList<>();

        for (DoctorSchedule schedule : schedules) {
            int bookedCount = countBookedAppointments(schedule);
            result.add(DoctorScheduleMapper.toResponse(schedule, bookedCount));
        }

        return result;
    }

    @Override
    public List<DoctorScheduleResponse> getCurrentDoctorSchedules(String username, Map<String, String> params) {
        Doctor doctor = getCurrentDoctor(username);
        Map<String, String> filters = params != null ? new HashMap<>(params) : new HashMap<>();

        String date = filters.get("date");
        if (date != null && !date.isBlank()) {
            filters.put("workDate", date);
        }

        filters.put("doctorId", doctor.getId().toString());
        filters.put("noPaging", "true");

        return this.scheduleRepo.getSchedules(filters)
                .stream()
                .map(DoctorScheduleMapper::toResponse)
                .toList();
    }

    @Override
    public DoctorSchedule getScheduleById(int id) {
        return this.scheduleRepo.getScheduleById(id);
    }

    @Override
    public DoctorSchedule createSchedule(DoctorScheduleRequest req) {
        validateSchedule(req, null);

        Doctor doctor = loadValidDoctor(req.getDoctorId());

        DoctorSchedule schedule = DoctorScheduleMapper.toEntityForCreate(req, doctor);

        this.scheduleRepo.addSchedule(schedule);

        return schedule;
    }

    @Override
    public DoctorSchedule updateSchedule(int id, DoctorScheduleRequest req) {
        DoctorSchedule existing = this.scheduleRepo.getScheduleById(id);

        if (existing == null) {
            throw new IllegalArgumentException("Lịch làm việc không tồn tại");
        }

        if (Boolean.FALSE.equals(existing.getActive())) {
            throw new IllegalArgumentException("Lịch làm việc đã bị xóa");
        }

        validateSchedule(req, existing.getId());

        Doctor doctor = loadValidDoctor(req.getDoctorId());

        DoctorScheduleMapper.updateEntity(existing, req, doctor);

        this.scheduleRepo.updateSchedule(existing);

        return existing;
    }

    @Override
    public void softDelete(int id) {
        DoctorSchedule existing = this.scheduleRepo.getScheduleById(id);

        if (existing == null) {
            throw new IllegalArgumentException("Lịch làm việc không tồn tại");
        }

        existing.setActive(false);
        existing.setStatus(DoctorScheduleStatus.CANCELLED.getCode());

        this.scheduleRepo.updateSchedule(existing);
    }

    @Override
    public long getTotalPages(Map<String, String> params) {
        return this.scheduleRepo.getTotalPages(params);
    }

    private int countBookedAppointments(DoctorSchedule schedule) {
        if (schedule == null || schedule.getDoctorId() == null || schedule.getWorkDate() == null) {
            return 0;
        }

        return (int) this.appointmentRepo.countBookedAppointmentsByDoctorAndDateAndWindow(
                schedule.getDoctorId().getId(),
                Date.valueOf(schedule.getWorkDate()),
                Time.valueOf(schedule.getStartTime()),
                Time.valueOf(schedule.getEndTime())
        );
    }

    private Doctor loadValidDoctor(Long doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("Vui lòng chọn bác sĩ");
        }

        Doctor doctor = this.doctorRepo.getDoctorById(doctorId.intValue());

        if (doctor == null || Boolean.FALSE.equals(doctor.getActive())) {
            throw new IllegalArgumentException("Bác sĩ không tồn tại hoặc đã ngưng hoạt động");
        }

        return doctor;
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

    private void validateSchedule(DoctorScheduleRequest req, Long excludeId) {
        if (req.getDoctorId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn bác sĩ");
        }

        if (req.getWorkDate() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày làm việc");
        }

        if (req.getWorkDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể tạo lịch trong quá khứ");
        }

        if (req.getStartTime() == null || req.getEndTime() == null) {
            throw new IllegalArgumentException("Vui lòng nhập giờ bắt đầu và giờ kết thúc");
        }

        if (!req.getStartTime().isBefore(req.getEndTime())) {
            throw new IllegalArgumentException("Giờ bắt đầu phải nhỏ hơn giờ kết thúc");
        }

        if (req.getMaxPatients() == null) {
            req.setMaxPatients(20);
        }

        if (req.getMaxPatients() <= 0) {
            throw new IllegalArgumentException("Số bệnh nhân tối đa phải lớn hơn 0");
        }

        req.setStatus(DoctorScheduleStatus.normalize(req.getStatus()));

        if (req.getNote() != null) {
            req.setNote(req.getNote().trim());
        }

        boolean overlapped = this.scheduleRepo.existsOverlappingSchedule(
                req.getDoctorId(),
                req.getWorkDate(),
                req.getStartTime(),
                req.getEndTime(),
                excludeId
        );

        if (overlapped) {
            throw new IllegalArgumentException("Khung giờ này bị trùng với lịch làm việc khác của bác sĩ");
        }
    }
}
