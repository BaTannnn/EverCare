package com.evercare.controllers.admin;

import com.evercare.dtos.request.MedicalServiceRequest;
import com.evercare.enums.MedicalServiceType;
import com.evercare.pojo.MedicalService;
import com.evercare.services.DepartmentService;
import com.evercare.services.MedicalServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping("/admin/services")
public class MedicalServiceController {

    @Autowired
    private MedicalServiceService medicalServiceService;

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public String index(@RequestParam Map<String, String> params, Model model) {
        model.addAttribute("services", this.medicalServiceService.getServices(params));
        model.addAttribute("departments", this.departmentService.getDepartments(Collections.emptyMap()));
        model.addAttribute("serviceTypes", MedicalServiceType.values());

        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("departmentId", params.getOrDefault("departmentId", ""));
        model.addAttribute("serviceType", params.getOrDefault("serviceType", ""));
        model.addAttribute("pages", this.medicalServiceService.getTotalPages(params));
        int page = Integer.parseInt(params.getOrDefault("page", "1"));
        model.addAttribute("page", page);
        return "services/services";
    }

    @GetMapping("/create")
    public String createView(Model model) {
        model.addAttribute("service", new MedicalServiceRequest());
        model.addAttribute("departments", this.departmentService.getDepartments(Collections.emptyMap()));
        model.addAttribute("serviceTypes", MedicalServiceType.values());
        model.addAttribute("isEdit", false);

        return "services/serviceDetail";
    }

    @GetMapping("/{serviceId}/edit")
    public String updateView(@PathVariable("serviceId") int id, Model model) {
        MedicalService service = this.medicalServiceService.getServiceById(id);

        if (service == null || Boolean.FALSE.equals(service.getActive())) {
            return "redirect:/admin/services";
        }

        MedicalServiceRequest form = new MedicalServiceRequest(
                service.getName(),
                service.getDescription(),
                service.getPrice(),
                service.getServiceType(),
                service.getDepartmentId() != null ? service.getDepartmentId().getId() : null
        );

        model.addAttribute("service", form);
        model.addAttribute("serviceId", id);
        model.addAttribute("departments", this.departmentService.getDepartments(Collections.emptyMap()));
        model.addAttribute("serviceTypes", MedicalServiceType.values());
        model.addAttribute("isEdit", true);

        return "services/serviceDetail";
    }

    @PostMapping
    public String create(
            @ModelAttribute("service") MedicalServiceRequest req,
            Model model
    ) {
        try {
            this.medicalServiceService.createService(req);
            return "redirect:/admin/services";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("err", ex.getMessage());
            model.addAttribute("departments", this.departmentService.getDepartments(Collections.emptyMap()));
            model.addAttribute("serviceTypes", MedicalServiceType.values());
            model.addAttribute("isEdit", false);

            return "services/serviceDetail";
        }
    }

    @PostMapping("/{serviceId}/edit")
    public String update(
            @PathVariable("serviceId") int id,
            @ModelAttribute("service") MedicalServiceRequest req,
            Model model
    ) {
        try {
            this.medicalServiceService.updateService(id, req);
            return "redirect:/admin/services";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("err", ex.getMessage());
            model.addAttribute("departments", this.departmentService.getDepartments(Collections.emptyMap()));
            model.addAttribute("serviceTypes", MedicalServiceType.values());
            model.addAttribute("serviceId", id);
            model.addAttribute("isEdit", true);

            return "services/serviceDetail";
        }
    }

    @PostMapping("/{serviceId}/delete")
    public String delete(@PathVariable("serviceId") int id) {
        this.medicalServiceService.softDelete(id);
        return "redirect:/admin/services";
    }
}