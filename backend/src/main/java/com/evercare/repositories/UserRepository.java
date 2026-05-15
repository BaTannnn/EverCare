/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.repositories;

import com.evercare.pojo.Users;

/**
 *
 * @author huu-thanhduong
 */
public interface UserRepository {
    Users getUserByUsername(String username);
    Users addUser(Users u);
    boolean authenticate(String username, String password);
}
