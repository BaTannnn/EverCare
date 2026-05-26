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
    User addUser(User u);
    boolean authenticate(String username, String password);
}
