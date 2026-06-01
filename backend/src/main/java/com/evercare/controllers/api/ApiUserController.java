/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.controllers.api;

import com.evercare.dtos.request.UserRegisterRequest;
import com.evercare.dtos.request.UserProfileUpdateRequest;
import com.evercare.dtos.request.LoginRequest;
import com.evercare.dtos.response.UserRegisterResponse;
import com.evercare.services.UserService;
import com.evercare.utils.JwtUtils;
import java.security.Principal;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author cadic
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiUserController {
    @Autowired
    private UserService userService;

    @PostMapping(path = {"/users", "/auth/register"})
    public ResponseEntity<UserRegisterResponse> create(@ModelAttribute UserRegisterRequest request) {
        UserRegisterResponse response = this.userService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) throws Exception {
        if (!this.userService.authenticate(request.getUsername(), request.getPassword())) {
            throw new com.evercare.exceptions.AuthenticationRequiredException("Sai thông tin đăng nhập");
        }

        String token = JwtUtils.generateToken(request.getUsername());
        return ResponseEntity.ok().body(Collections.singletonMap("token", token));
    }

    @GetMapping("/secure/profile")
    public ResponseEntity<UserRegisterResponse> getProfile(Principal principal) {
        return new ResponseEntity<>(this.userService.getUserProfile(requireUsername(principal)), HttpStatus.OK);
    }

    @PutMapping(path = "/secure/profile")
    public ResponseEntity<UserRegisterResponse> updateProfile(
            Principal principal,
            @ModelAttribute UserProfileUpdateRequest request
    ) {
        UserRegisterResponse response = this.userService.updateUserProfile(requireUsername(principal), request);
        return ResponseEntity.ok(response);
    }

    private String requireUsername(Principal principal) {
        if (principal == null) {
            throw new com.evercare.exceptions.AuthenticationRequiredException("Vui lòng đăng nhập");
        }
        return principal.getName();
    }
}
