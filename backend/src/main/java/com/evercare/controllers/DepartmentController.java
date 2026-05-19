/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.controllers;

import com.evercare.pojo.Department;
import com.evercare.services.DepartmentService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
        model.addAttribute("departments", departmentService.getDeparments(params));
        return "departments/departments";
    }
    
    @GetMapping("/departments/create")
    public String createView(Model model) {
        model.addAttribute("department", new Department());
        return "departments/departmentDetail";
    }
    
    @GetMapping("departments/{departmentsId}")
    public String updateView(@PathVariable(value = "departmentsId") int id,
            Model model) {
        model.addAttribute("department", this.departmentService.getDeparmentById(id));
        
        return "departments/departmentDetail";
    }
    
    @PostMapping("/departments")
    public String create(Model model, @ModelAttribute(value = "department") Department d) {
        try {
            this.departmentService.addOrUpdateDepartment(d);
            return "redirect:/admin/departments";
        } catch (Exception e) {
            model.addAttribute("err", e.getMessage());
            return "departments/departmentDetail";
        }
    }
    
    @PostMapping("/departments/{departmentId}/delete")
    public String delete(Model model, @PathVariable(value = "departmentId") int id) {
        try {
            this.departmentService.sotfDelete(id);
            return "redirect:/admin/departments";
        } catch (Exception e) {
            model.addAttribute("err", e.getMessage());
            return "departments/departmentDetail";
        }
    }
}
