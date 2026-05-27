/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.repositories;

import com.evercare.pojo.User;

/**
 *
 * @author huu-thanhduong
 */
public interface UserRepository {
    User getUserByUsername(String username);
    User findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    User save(User u);
    boolean authenticate(String username, String password);
}
