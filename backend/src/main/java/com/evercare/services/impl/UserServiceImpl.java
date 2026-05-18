/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.evercare.pojo.Role;
import com.evercare.pojo.User;
import com.evercare.repositories.RoleRepository;
import com.evercare.repositories.UserRepository;
import com.evercare.services.UserService;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author cadic
 */
@Service("userDetailsService")
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private RoleRepository roleRepo;

    @Override
    public User getUserByUsername(String username) {
        return userRepo.getUserByUsername(username);
    }

    @Override
    public User addUser(Map<String, String> params, MultipartFile avatar) {
        User u = new User();
        u.setFullName(params.get("fullName"));
        u.setPhone(params.get("phone"));
        u.setEmail(params.get("email"));
        u.setUsername(params.get("username"));
        u.setPassword(passwordEncoder.encode(params.get("password")));
        u.setRoleSet(new HashSet<>(Set.of(roleRepo.getRoleByRoleName("ROLE_PATIENT"))));
        u.setActive(true);
        u.setEnabled(true);
        u.setAccountNonLocked(true);

        Date now = new Date();
        u.setCreatedAt(now);
        u.setUpdatedAt(now);

        if (!avatar.isEmpty()) {
            try {
                Map res = this.cloudinary.uploader().upload(avatar.getBytes(),
                        ObjectUtils.asMap("resource_type", "auto"));
                u.setAvatarUrl(res.get("secure_url").toString());
            } catch (IOException ex) {
                Logger.getLogger(UserServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return this.userRepo.addUser(u);
    }

    @Override
    public boolean authenticate(String username, String password) {
        return this.userRepo.authenticate(username, password);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepo.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Không tồn tại!");
        }
        
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : user.getRoleSet()) {
            System.out.println(role.getCode());
            authorities.add(new SimpleGrantedAuthority(role.getCode()));
        }
        
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(), authorities);
    }
    
}


    
