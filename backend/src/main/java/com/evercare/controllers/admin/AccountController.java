package com.evercare.controllers.admin;

import com.evercare.dtos.request.AccountRequest;
import com.evercare.services.UserService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AccountController {

    @Autowired
    private UserService userService;

    @GetMapping("/accounts")
    public String index(@RequestParam Map<String, String> params, Model model) {
        Map<String, String> listParams = new HashMap<>(params);
        listParams.putIfAbsent("page", "1");

        model.addAttribute("accounts", this.userService.getAccounts(listParams));
        model.addAttribute("roles", this.userService.getActiveRoles());
        model.addAttribute("kw", listParams.getOrDefault("kw", ""));
        model.addAttribute("roleCode", listParams.getOrDefault("roleCode", ""));
        model.addAttribute("pages", this.userService.getTotalPagesForAccounts(listParams));
        model.addAttribute("page", Integer.parseInt(listParams.getOrDefault("page", "1")));
        return "accounts/accounts";
    }

    @GetMapping("/accounts/create")
    public String createView(Model model) {
        model.addAttribute("account", new AccountRequest());
        model.addAttribute("roles", this.userService.getActiveRoles());
        model.addAttribute("pageTitle", "Thêm tài khoản");
        return "accounts/accountDetail";
    }

    @PostMapping("/accounts")
    public String create(@ModelAttribute("account") AccountRequest req, Model model, RedirectAttributes redirectAttributes) {
        try {
            com.evercare.dtos.response.AccountResponse created = this.userService.createAccount(req);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo tài khoản thành công");
            redirectAttributes.addFlashAttribute("generatedUsername", created.getUsername());
            redirectAttributes.addFlashAttribute("generatedPassword", created.getRawPassword());
            redirectAttributes.addFlashAttribute("generatedRole", created.getRoleCode());
            return "redirect:/admin/accounts";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("err", ex.getMessage());
            model.addAttribute("account", req);
            model.addAttribute("roles", this.userService.getActiveRoles());
            model.addAttribute("pageTitle", "Thêm tài khoản");
            return "accounts/accountDetail";
        }
    }

    @PostMapping("/accounts/{accountId}/delete")
    public String delete(@PathVariable("accountId") Long id) {
        this.userService.deactivateAccount(id);
        return "redirect:/admin/accounts";
    }
}
