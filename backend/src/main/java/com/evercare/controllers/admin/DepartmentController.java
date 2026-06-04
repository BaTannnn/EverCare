/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.controllers.admin;

import com.evercare.dtos.request.DepartmentRequest;
import com.evercare.pojo.Department;
import com.evercare.services.DepartmentService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author cadic
 */
@Controller
@RequestMapping("/admin")
public class DepartmentController {
    @Autowired DepartmentService departmentService;
    
    @GetMapping("/departments")
    public String departmentView(Model model, @RequestParam Map<String, String> params) {
        Map<String, String> listParams = new HashMap<>(params);
        listParams.putIfAbsent("page", "1");

        model.addAttribute("departments", departmentService.getDepartments(listParams));
        model.addAttribute("kw", listParams.getOrDefault("kw", ""));
        model.addAttribute("pages", this.departmentService.getTotalPages(listParams));

        int page = Integer.parseInt(listParams.getOrDefault("page", "1"));
        model.addAttribute("page", page);
        return "departments/departments";
    }
    
    @GetMapping("/departments/create")
    public String createView(Model model) {
        model.addAttribute("department", new DepartmentRequest());
        return "departments/departmentDetail";
    }
    
    @GetMapping("departments/{departmentsId}")
    public String updateView(@PathVariable(value = "departmentsId") int id,
            Model model) {

        Department department = this.departmentService.getDepartmentById(id);

        if (department == null || Boolean.FALSE.equals(department.getActive())) {
            return "redirect:/admin/departments";
        }

        DepartmentRequest form = new DepartmentRequest(
                department.getName(),
                department.getDescription()
        );

        model.addAttribute("department", form);
        model.addAttribute("departmentId", id);
        model.addAttribute("isEdit", true);
        
        return "departments/departmentDetail";
    }
    
    @PostMapping("/departments")
    public String create(Model model, @ModelAttribute(value = "department") DepartmentRequest req) {
        try {
            this.departmentService.createDepartment(req);
            return "redirect:/admin/departments";
        } catch (Exception e) {
            model.addAttribute("err", e.getMessage());
            model.addAttribute("isEdit", false);
            return "departments/departmentDetail";
        }
    }

    @PostMapping("/departments/{departmentId}/edit")
    public String update(
            @PathVariable("departmentId") int id,
            Model model,
            @ModelAttribute("department") DepartmentRequest req
    ) {
        try {
            this.departmentService.updateDepartment(id, req);
            return "redirect:/admin/departments";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("err", ex.getMessage());
            model.addAttribute("pageTitle", "Cập nhật khoa");
            model.addAttribute("departmentId", id);
            model.addAttribute("isEdit", true);
            return "departments/departmentDetail";
        }
    }
    
    @PostMapping("/departments/{departmentId}/delete")
    public String delete(Model model, @PathVariable(value = "departmentId") int id) {
        try {
            this.departmentService.softDelete(id);
            return "redirect:/admin/departments";
        } catch (Exception e) {
            model.addAttribute("err", e.getMessage());
            return "departments/departmentDetail";
        }
    }
}
