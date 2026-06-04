package com.evercare.controllers.admin;

import com.evercare.dtos.request.DoctorRequest;
import com.evercare.enums.DoctorType;
import com.evercare.enums.DoctorWorkStatus;
import com.evercare.pojo.Doctor;
import com.evercare.services.DepartmentService;
import com.evercare.services.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public String index(@RequestParam Map<String, String> params, Model model) {
        Map<String, String> listParams = new HashMap<>(params);
        listParams.putIfAbsent("page", "1");

        model.addAttribute("doctors", this.doctorService.getDoctors(listParams));
        model.addAttribute("departments", this.departmentService.getDepartments(Collections.emptyMap()));
        model.addAttribute("doctorTypes", DoctorType.values());
        model.addAttribute("workStatuses", DoctorWorkStatus.values());

        model.addAttribute("kw", listParams.getOrDefault("kw", ""));
        model.addAttribute("departmentId", listParams.getOrDefault("departmentId", ""));
        model.addAttribute("doctorType", listParams.getOrDefault("doctorType", ""));
        model.addAttribute("workStatus", listParams.getOrDefault("workStatus", ""));
        model.addAttribute("pages", this.doctorService.getTotalPages(listParams));

        int page = Integer.parseInt(listParams.getOrDefault("page", "1"));
        model.addAttribute("page", page);
        return "doctors/doctors";
    }

    @GetMapping("/create")
    public String createView(Model model) {
        model.addAttribute("doctor", new DoctorRequest());
        model.addAttribute("users", this.doctorService.getSelectableUsers(null));
        model.addAttribute("departments", this.departmentService.getDepartments(Collections.emptyMap()));
        model.addAttribute("doctorTypes", DoctorType.values());
        model.addAttribute("workStatuses", DoctorWorkStatus.values());
        model.addAttribute("pageTitle", "Thêm bác sĩ");
        model.addAttribute("isEdit", false);

        return "doctors/doctorDetail";
    }

    @GetMapping("/{doctorId}/edit")
    public String updateView(@PathVariable("doctorId") int id, Model model) {
        Doctor doctor = this.doctorService.getDoctorById(id);

        if (doctor == null || Boolean.FALSE.equals(doctor.getActive())) {
            return "redirect:/admin/doctors";
        }

        DoctorRequest form = new DoctorRequest();
        form.setDepartmentId(doctor.getDepartmentId() != null ? doctor.getDepartmentId().getId() : null);
        form.setUserId(doctor.getUserId() != null ? doctor.getUserId().getId() : null);
        form.setFullName(doctor.getFullName());
        form.setPhone(doctor.getPhone());
        form.setEmail(doctor.getEmail());
        form.setQualification(doctor.getQualification());
        form.setSpecialization(doctor.getSpecialization());
        form.setDoctorType(doctor.getDoctorType());
        form.setWorkStatus(doctor.getWorkStatus());
        form.setBaseSalary(doctor.getBaseSalary());
        form.setHourlyRate(doctor.getHourlyRate());
        form.setBio(doctor.getBio());

        model.addAttribute("doctor", form);
        model.addAttribute("doctorId", id);
        model.addAttribute("currentAvatarUrl", doctor.getAvatarUrl());
        model.addAttribute("users", this.doctorService.getSelectableUsers(form.getUserId()));
        model.addAttribute("departments", this.departmentService.getDepartments(Collections.emptyMap()));
        model.addAttribute("doctorTypes", DoctorType.values());
        model.addAttribute("workStatuses", DoctorWorkStatus.values());
        model.addAttribute("pageTitle", "Cập nhật bác sĩ");
        model.addAttribute("isEdit", true);

        return "doctors/doctorDetail";
    }

    @PostMapping
    public String create(@ModelAttribute("doctor") DoctorRequest req, Model model) {
        try {
            this.doctorService.createDoctor(req);
            return "redirect:/admin/doctors";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("err", ex.getMessage());
            model.addAttribute("doctor", req);
            model.addAttribute("users", this.doctorService.getSelectableUsers(req.getUserId()));
            model.addAttribute("departments", this.departmentService.getDepartments(Collections.emptyMap()));
            model.addAttribute("doctorTypes", DoctorType.values());
            model.addAttribute("workStatuses", DoctorWorkStatus.values());
            model.addAttribute("pageTitle", "Thêm bác sĩ");
            model.addAttribute("isEdit", false);

            return "doctors/doctorDetail";
        }
    }

    @PostMapping("/{doctorId}/edit")
    public String update(
            @PathVariable("doctorId") int id,
            @ModelAttribute("doctor") DoctorRequest req,
            Model model
    ) {
        try {
            this.doctorService.updateDoctor(id, req);
            return "redirect:/admin/doctors";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("err", ex.getMessage());
            model.addAttribute("doctor", req);
            model.addAttribute("doctorId", id);
            model.addAttribute("users", this.doctorService.getSelectableUsers(req.getUserId()));
            model.addAttribute("departments", this.departmentService.getDepartments(Collections.emptyMap()));
            model.addAttribute("doctorTypes", DoctorType.values());
            model.addAttribute("workStatuses", DoctorWorkStatus.values());
            model.addAttribute("pageTitle", "Cập nhật bác sĩ");
            model.addAttribute("isEdit", true);

            return "doctors/doctorDetail";
        }
    }

    @PostMapping("/{doctorId}/delete")
    public String delete(@PathVariable("doctorId") int id) {
        this.doctorService.softDelete(id);
        return "redirect:/admin/doctors";
    }
}
