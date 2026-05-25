package com.evercare.services.impl;

import com.evercare.dtos.request.DoctorScheduleRequest;
import com.evercare.enums.DoctorScheduleStatus;
import com.evercare.mappers.DoctorScheduleMapper;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.DoctorSchedule;
import com.evercare.repositories.DoctorRepository;
import com.evercare.repositories.DoctorScheduleRepository;
import com.evercare.services.DoctorScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    @Autowired
    private DoctorScheduleRepository scheduleRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Override
    public List<DoctorSchedule> getSchedules(Map<String, String> params) {
        return this.scheduleRepo.getSchedules(params);
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