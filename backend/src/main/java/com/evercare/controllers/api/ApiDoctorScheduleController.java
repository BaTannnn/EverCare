package com.evercare.controllers.api;

import com.evercare.dtos.response.DoctorScheduleResponse;
import com.evercare.services.DoctorScheduleService;
import java.security.Principal;
import java.time.LocalDate;
import com.evercare.pojo.Doctor;
import java.util.List;
import java.util.Map;
import com.evercare.services.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiDoctorScheduleController {
    @Autowired
    private DoctorScheduleService scheduleService;
    @Autowired
    private DoctorService doctorService;

    @GetMapping({"/doctors/schedules", "/doctor/schedules"})
    public ResponseEntity<?> list(
            Principal principal,
            @RequestParam Map<String, String> params
    ) {
        return currentDoctorSchedules(principal, params);
    }

    @GetMapping({"/doctors/schedules/today", "/doctor/schedules/today"})
    public ResponseEntity<?> today(Principal principal) {
        return currentDoctorSchedules(principal, Map.of("date", LocalDate.now().toString()));
    }

    private ResponseEntity<?> currentDoctorSchedules(Principal principal, Map<String, String> params) {
        List<DoctorScheduleResponse> result = this.scheduleService
                .getCurrentDoctorSchedules(requireUsername(principal), params);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/doctors/{doctorId}/schedules")
    public ResponseEntity<?> list (
            @PathVariable("doctorId") Long doctorId,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to
    ){
        Doctor doctor = this.doctorService.getDoctorById(doctorId.intValue());

        if (doctor == null || Boolean.FALSE.equals(doctor.getActive())) {
            throw new java.util.NoSuchElementException("Bác sĩ không tồn tại");
        }

        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = fromDate.plusDays(7);

        if (from != null && !from.isBlank()) {
            fromDate = LocalDate.parse(from);
            toDate = fromDate.plusDays(7);
        }

        if (to != null && !to.isBlank()) {
            toDate = LocalDate.parse(to);
        }

        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu");
        }

        List<DoctorScheduleResponse> result = this.scheduleService
                .listAvailableSchedulesByDoctorId(doctorId, fromDate, toDate);

        return ResponseEntity.ok(result);
    }

    private String requireUsername(Principal principal) {
        if (principal == null) {
            throw new com.evercare.exceptions.AuthenticationRequiredException("Vui lòng đăng nhập");
        }
        return principal.getName();
    }
}
