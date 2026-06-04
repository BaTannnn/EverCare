/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.repositories;

import com.evercare.pojo.User;
import java.util.List;
import java.util.Map;

/**
 *
 * @author huu-thanhduong
 */
public interface UserRepository {
    User getUserByUsername(String username);
    User findByUsername(String username);
    User findById(Long id);
    List<User> getUsers(Map<String, String> params);
    List<User> getActiveUsers();
    long countUsers(Map<String, String> params);
    long getTotalPages(Map<String, String> params);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    User save(User u);
    User update(User u);
    boolean authenticate(String username, String password);
}
