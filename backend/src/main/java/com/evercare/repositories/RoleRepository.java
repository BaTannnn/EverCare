/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.repositories;

import com.evercare.pojo.Role;

/**
 *
 * @author cadic
 */
public interface RoleRepository {
    Role getRoleByRoleName(String roleName);
}
