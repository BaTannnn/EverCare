/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.evercare.dtos.request.UserRegisterRequest;
import com.evercare.dtos.request.UserProfileUpdateRequest;
import com.evercare.dtos.response.UserRegisterResponse;
import com.evercare.pojo.Role;
import com.evercare.pojo.User;
import com.evercare.mappers.UserMapper;
import com.evercare.repositories.RoleRepository;
import com.evercare.repositories.UserRepository;
import com.evercare.services.UserService;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
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
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{4,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

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
        return userRepo.findByUsername(username);
    }

    @Override
    public UserRegisterResponse getUserProfile(String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }

        return UserMapper.toResponse(user, user.getPatient() != null);
    }

    @Override
    @Transactional
    public UserRegisterResponse registerUser(UserRegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu đăng ký không hợp lệ");
        }

        String username = normalizeUsername(request.getUsername());
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();
        String email = normalizeEmail(request.getEmail());
        String phone = normalizePhone(request.getPhone());
        String fullName = normalizeFullName(request.getFullName());

        validateUsername(username);
        validatePassword(password, confirmPassword);
        validateEmail(email);
        validatePhone(phone);
        validateFullName(fullName);

        if (userRepo.existsByUsername(username)) {
            throw new IllegalStateException("Username đã tồn tại");
        }
        if (userRepo.existsByEmail(email)) {
            throw new IllegalStateException("Email đã tồn tại");
        }
        if (userRepo.existsByPhone(phone)) {
            throw new IllegalStateException("Số điện thoại đã tồn tại");
        }

        Role patientRole = ensurePatientRole();

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setPhone(phone);
        user.setFullName(fullName);
        HashSet<Role> roles = new HashSet<>();
        roles.add(patientRole);
        user.setRoleSet(roles);
        if (patientRole.getUserSet() == null) {
            patientRole.setUserSet(new HashSet<>());
        }
        patientRole.getUserSet().add(user);
        user.setActive(true);
        user.setEnabled(true);
        user.setAccountNonLocked(true);

        Date now = new Date();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            user.setAvatarUrl(uploadAvatar(request.getAvatar()));
        }

        User saved = userRepo.save(user);
        return UserMapper.toResponse(saved, false);
    }

    @Override
    @Transactional
    public UserRegisterResponse updateUserProfile(String username, UserProfileUpdateRequest request) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Vui lòng đăng nhập");
        }
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu cập nhật không hợp lệ");
        }

        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }

        String email = normalizeNullableEmail(request.getEmail());
        String phone = normalizeNullablePhone(request.getPhone());
        String fullName = normalizeNullableFullName(request.getFullName());

        if (email != null) {
            validateEmail(email);
            if (!email.equalsIgnoreCase(user.getEmail()) && userRepo.existsByEmail(email)) {
                throw new IllegalStateException("Email đã tồn tại");
            }
            user.setEmail(email);
        }

        if (phone != null) {
            validatePhone(phone);
            if (!phone.equals(user.getPhone()) && userRepo.existsByPhone(phone)) {
                throw new IllegalStateException("Số điện thoại đã tồn tại");
            }
            user.setPhone(phone);
        }

        if (fullName != null) {
            validateFullName(fullName);
            user.setFullName(fullName);
        }

        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String avatarUrl = uploadAvatar(request.getAvatar());
            if (avatarUrl != null) {
                user.setAvatarUrl(avatarUrl);
            }
        }

        user.setUpdatedAt(new Date());
        User saved = userRepo.update(user);
        return UserMapper.toResponse(saved, saved.getPatient() != null);
    }

    @Override
    public boolean authenticate(String username, String password) {
        return this.userRepo.authenticate(username, password);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Không tồn tại!");
        }
        
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (user.getRoleSet() != null) {
            for (Role role : user.getRoleSet()) {
                authorities.add(new SimpleGrantedAuthority(role.getCode()));
            }
        }
        
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(), authorities);
    }

    private String uploadAvatar(MultipartFile avatar) {
        try {
            Map res = this.cloudinary.uploader().upload(avatar.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            return res.get("secure_url").toString();
        } catch (Exception ex) {
            return null;
        }
    }

    private Role ensurePatientRole() {
        Role patientRole = roleRepo.findByCode("ROLE_PATIENT");
        if (patientRole != null) {
            return patientRole;
        }

        Role role = new Role();
        role.setCode("ROLE_PATIENT");
        role.setName("Patient");
        role.setDescription("Role for patient account");
        role.setActive(true);

        Date now = new Date();
        role.setCreatedAt(now);
        role.setUpdatedAt(now);

        return roleRepo.save(role);
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullableEmail(String email) {
        if (email == null || email.trim().isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.trim();
    }

    private String normalizeNullablePhone(String phone) {
        if (phone == null || phone.trim().isBlank()) {
            return null;
        }
        return phone.trim();
    }

    private String normalizeFullName(String fullName) {
        return fullName == null ? null : fullName.trim();
    }

    private String normalizeNullableFullName(String fullName) {
        if (fullName == null || fullName.trim().isBlank()) {
            return null;
        }
        return fullName.trim();
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username là bắt buộc");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username phải từ 4 đến 50 ký tự, không chứa khoảng trắng");
        }
    }

    private void validatePassword(String password, String confirmPassword) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password là bắt buộc");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password phải có ít nhất 8 ký tự");
        }
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password và confirmPassword không khớp");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email là bắt buộc");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone là bắt buộc");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Phone phải gồm đúng 10 chữ số");
        }
    }

    private void validateFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name là bắt buộc");
        }
        if (fullName.length() < 2 || fullName.length() > 100) {
            throw new IllegalArgumentException("Full name phải từ 2 đến 100 ký tự");
        }
    }
    
}


    
