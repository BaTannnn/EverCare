/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.repositories;

import com.evercare.pojo.Role;
import java.util.List;

/**
 *
 * @author cadic
 */
public interface RoleRepository {
    Role findByCode(String code);
    List<Role> getActiveRoles();
    Role save(Role role);
}
