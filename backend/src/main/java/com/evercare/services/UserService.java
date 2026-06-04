/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.services;

import com.evercare.dtos.request.UserRegisterRequest;
import com.evercare.dtos.request.UserProfileUpdateRequest;
import com.evercare.dtos.request.AccountRequest;
import com.evercare.dtos.response.AccountResponse;
import com.evercare.dtos.response.UserRegisterResponse;
import com.evercare.pojo.Role;
import com.evercare.pojo.User;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author huu-thanhduong
 */
public interface UserService extends UserDetailsService {
    User getUserByUsername(String username);
    User getUserById(Long id);
    UserRegisterResponse getUserProfile(String username);
    UserRegisterResponse registerUser(UserRegisterRequest request);
    UserRegisterResponse updateUserProfile(String username, UserProfileUpdateRequest request);
    List<User> getAccounts(Map<String, String> params);
    long getTotalPagesForAccounts(Map<String, String> params);
    List<Role> getActiveRoles();
    List<User> getDoctorLinkUsers(Long currentUserId);
    List<User> getEmployeeLinkUsers(Long currentUserId);
    AccountResponse createAccount(AccountRequest request);
    void deactivateAccount(Long id);
    boolean authenticate(String username, String password);
}
