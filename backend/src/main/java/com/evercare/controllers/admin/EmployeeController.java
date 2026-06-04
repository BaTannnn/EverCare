package com.evercare.controllers.admin;

import com.evercare.dtos.request.EmployeeRequest;
import com.evercare.pojo.Employee;
import com.evercare.services.EmployeeService;
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

@Controller
@RequestMapping("/admin")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/employees")
    public String index(@RequestParam Map<String, String> params, Model model) {
        Map<String, String> listParams = new HashMap<>(params);
        listParams.putIfAbsent("page", "1");

        model.addAttribute("employees", this.employeeService.getEmployees(listParams));
        model.addAttribute("kw", listParams.getOrDefault("kw", ""));
        model.addAttribute("pages", this.employeeService.getTotalPages(listParams));
        model.addAttribute("page", Integer.parseInt(listParams.getOrDefault("page", "1")));
        return "employees/employees";
    }

    @GetMapping("/employees/create")
    public String createView(Model model) {
        model.addAttribute("employee", new EmployeeRequest());
        model.addAttribute("users", this.employeeService.getSelectableUsers(null));
        model.addAttribute("pageTitle", "Thêm nhân viên");
        model.addAttribute("isEdit", false);
        return "employees/employeeDetail";
    }

    @GetMapping("/employees/{employeeId}")
    public String updateView(@PathVariable("employeeId") int id, Model model) {
        Employee employee = this.employeeService.getEmployeeById(id);
        if (employee == null || Boolean.FALSE.equals(employee.getActive())) {
            return "redirect:/admin/employees";
        }

        EmployeeRequest form = new EmployeeRequest();
        form.setEmployeeCode(employee.getEmployeeCode());
        form.setUserId(employee.getUserId() != null ? employee.getUserId().getId() : null);
        form.setFullName(employee.getFullName());
        form.setGender(employee.getGender());
        form.setDateOfBirth(employee.getDateOfBirth() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(employee.getDateOfBirth()) : null);
        form.setPhone(employee.getPhone());
        form.setEmail(employee.getEmail());
        form.setAddress(employee.getAddress());
        form.setPosition(employee.getPosition());
        form.setSalary(employee.getSalary());
        form.setHiredDate(employee.getHiredDate() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(employee.getHiredDate()) : null);

        model.addAttribute("employee", form);
        model.addAttribute("employeeId", id);
        model.addAttribute("users", this.employeeService.getSelectableUsers(form.getUserId()));
        model.addAttribute("pageTitle", "Cập nhật nhân viên");
        model.addAttribute("isEdit", true);
        return "employees/employeeDetail";
    }

    @PostMapping("/employees")
    public String create(@ModelAttribute("employee") EmployeeRequest req, Model model) {
        try {
            this.employeeService.createEmployee(req);
            return "redirect:/admin/employees";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("err", ex.getMessage());
            model.addAttribute("employee", req);
            model.addAttribute("users", this.employeeService.getSelectableUsers(req.getUserId()));
            model.addAttribute("pageTitle", "Thêm nhân viên");
            model.addAttribute("isEdit", false);
            return "employees/employeeDetail";
        }
    }

    @PostMapping("/employees/{employeeId}/edit")
    public String update(@PathVariable("employeeId") int id,
                         @ModelAttribute("employee") EmployeeRequest req,
                         Model model) {
        try {
            this.employeeService.updateEmployee(id, req);
            return "redirect:/admin/employees";
        } catch (IllegalArgumentException ex) {
            Employee current = this.employeeService.getEmployeeById(id);
            Long selectedUserId = req.getUserId();
            if (selectedUserId == null && current != null && current.getUserId() != null) {
                selectedUserId = current.getUserId().getId();
                req.setUserId(selectedUserId);
            }

            model.addAttribute("err", ex.getMessage());
            model.addAttribute("employeeId", id);
            model.addAttribute("employee", req);
            model.addAttribute("users", this.employeeService.getSelectableUsers(selectedUserId));
            model.addAttribute("pageTitle", "Cập nhật nhân viên");
            model.addAttribute("isEdit", true);
            return "employees/employeeDetail";
        }
    }

    @PostMapping("/employees/{employeeId}/delete")
    public String delete(@PathVariable("employeeId") int id) {
        this.employeeService.softDelete(id);
        return "redirect:/admin/employees";
    }
}
