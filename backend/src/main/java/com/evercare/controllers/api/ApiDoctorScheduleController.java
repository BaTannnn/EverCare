package com.evercare.controllers.api;

import com.evercare.dtos.response.DoctorScheduleResponse;
import com.evercare.pojo.Doctor;
import com.evercare.services.DoctorService;
import com.evercare.services.DoctorScheduleService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private DoctorScheduleService doctorScheduleService;

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/doctors/{doctorId}/schedules")
    public ResponseEntity<?> list(
            @PathVariable("doctorId") Long doctorId,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to
    ) {

        Doctor doctor = this.doctorService.getDoctorById(doctorId.intValue());

        if (doctor == null || Boolean.FALSE.equals(doctor.getActive())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Bác sĩ không tồn tại"));
        }

        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = fromDate.plusDays(7);

        try {
            if (from != null && !from.isBlank()) {
                fromDate = LocalDate.parse(from);
                toDate = fromDate.plusDays(7);
            }

            if (to != null && !to.isBlank()) {
                toDate = LocalDate.parse(to);
            }
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ngày không hợp lệ, định dạng đúng là yyyy-MM-dd"));
        }

        if (toDate.isBefore(fromDate)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu"));
        }

        List<DoctorScheduleResponse> result = this.doctorScheduleService
                .listAvailableSchedulesByDoctorId(doctorId, fromDate, toDate);

        return ResponseEntity.ok(result);
    }
}
