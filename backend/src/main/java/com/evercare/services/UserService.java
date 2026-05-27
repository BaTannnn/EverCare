/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.services;

import com.evercare.dtos.request.UserRegisterRequest;
import com.evercare.dtos.response.UserRegisterResponse;
import com.evercare.pojo.User;
import java.util.Map;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author huu-thanhduong
 */
public interface UserService extends UserDetailsService {
    User getUserByUsername(String username);
    UserRegisterResponse getUserProfile(String username);
    UserRegisterResponse registerUser(UserRegisterRequest request);
    boolean authenticate(String username, String password);
}
