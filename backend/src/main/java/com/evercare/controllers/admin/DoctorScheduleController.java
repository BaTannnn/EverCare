package com.evercare.controllers.admin;

import com.evercare.dtos.request.DoctorScheduleRequest;
import com.evercare.enums.DoctorScheduleStatus;
import com.evercare.pojo.DoctorSchedule;
import com.evercare.services.DepartmentService;
import com.evercare.services.DoctorScheduleService;
import com.evercare.services.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping("/admin/schedules")
public class DoctorScheduleController {

    @Autowired
    private DoctorScheduleService scheduleService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public String index(@RequestParam Map<String, String> params, Model model) {
        model.addAttribute("schedules", this.scheduleService.getSchedules(params));
        model.addAttribute("doctors", this.doctorService.getDoctors(Collections.emptyMap()));
        model.addAttribute("departments", this.departmentService.getDepartments(Collections.emptyMap()));
        model.addAttribute("statuses", DoctorScheduleStatus.values());

        model.addAttribute("doctorId", params.getOrDefault("doctorId", ""));
        model.addAttribute("departmentId", params.getOrDefault("departmentId", ""));
        model.addAttribute("workDate", params.getOrDefault("workDate", ""));
        model.addAttribute("status", params.getOrDefault("status", ""));
        model.addAttribute("pages", this.doctorService.getTotalPages(params));

        int page = Integer.parseInt(params.getOrDefault("page", "1"));
        model.addAttribute("page", page);
        return "schedules/schedules";
    }

    @GetMapping("/create")
    public String createView(Model model) {
        model.addAttribute("schedule", new DoctorScheduleRequest());
        model.addAttribute("doctors", this.doctorService.getDoctors(Collections.emptyMap()));
        model.addAttribute("statuses", DoctorScheduleStatus.values());
        model.addAttribute("pageTitle", "Thêm lịch làm việc");
        model.addAttribute("isEdit", false);

        return "schedules/scheduleDetail";
    }

    @GetMapping("/{scheduleId}/edit")
    public String updateView(@PathVariable("scheduleId") int id, Model model) {
        DoctorSchedule schedule = this.scheduleService.getScheduleById(id);

        if (schedule == null || Boolean.FALSE.equals(schedule.getActive())) {
            return "redirect:/admin/schedules";
        }

        DoctorScheduleRequest form = new DoctorScheduleRequest(
                schedule.getDoctorId() != null ? schedule.getDoctorId().getId() : null,
                schedule.getWorkDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getMaxPatients(),
                schedule.getStatus(),
                schedule.getNote()
        );

        model.addAttribute("schedule", form);
        model.addAttribute("scheduleId", id);
        model.addAttribute("doctors", this.doctorService.getDoctors(Collections.emptyMap()));
        model.addAttribute("statuses", DoctorScheduleStatus.values());
        model.addAttribute("pageTitle", "Cập nhật lịch làm việc");
        model.addAttribute("isEdit", true);

        return "schedules/scheduleDetail";
    }

    @PostMapping
    public String create(@ModelAttribute("schedule") DoctorScheduleRequest req, Model model) {
        try {
            this.scheduleService.createSchedule(req);
            return "redirect:/admin/schedules";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("err", ex.getMessage());
            model.addAttribute("schedule", req);
            model.addAttribute("doctors", this.doctorService.getDoctors(Collections.emptyMap()));
            model.addAttribute("statuses", DoctorScheduleStatus.values());
            model.addAttribute("pageTitle", "Thêm lịch làm việc");
            model.addAttribute("isEdit", false);

            return "schedules/scheduleDetail";
        }
    }

    @PostMapping("/{scheduleId}/edit")
    public String update(@PathVariable("scheduleId") int id,
                         @ModelAttribute("schedule") DoctorScheduleRequest req,
                         Model model) {
        try {
            this.scheduleService.updateSchedule(id, req);
            return "redirect:/admin/schedules";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("err", ex.getMessage());
            model.addAttribute("schedule", req);
            model.addAttribute("scheduleId", id);
            model.addAttribute("doctors", this.doctorService.getDoctors(Collections.emptyMap()));
            model.addAttribute("statuses", DoctorScheduleStatus.values());
            model.addAttribute("pageTitle", "Cập nhật lịch làm việc");
            model.addAttribute("isEdit", true);

            return "schedules/scheduleDetail";
        }
    }

    @PostMapping("/{scheduleId}/delete")
    public String delete(@PathVariable("scheduleId") int id) {
        this.scheduleService.softDelete(id);
        return "redirect:/admin/schedules";
    }
}