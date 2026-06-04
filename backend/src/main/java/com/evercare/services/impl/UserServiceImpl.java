/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.evercare.dtos.request.AccountRequest;
import com.evercare.dtos.request.UserProfileUpdateRequest;
import com.evercare.dtos.request.UserRegisterRequest;
import com.evercare.dtos.response.AccountResponse;
import com.evercare.dtos.response.UserRegisterResponse;
import com.evercare.mappers.UserMapper;
import com.evercare.pojo.Role;
import com.evercare.pojo.User;
import com.evercare.repositories.RoleRepository;
import com.evercare.repositories.UserRepository;
import com.evercare.services.UserService;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789".toCharArray();

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
    public User getUserById(Long id) {
        return this.userRepo.findById(id);
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
    public List<User> getAccounts(Map<String, String> params) {
        return this.userRepo.getUsers(params);
    }

    @Override
    public long getTotalPagesForAccounts(Map<String, String> params) {
        return this.userRepo.getTotalPages(params);
    }

    @Override
    public List<Role> getActiveRoles() {
        return this.roleRepo.getActiveRoles();
    }

    @Override
    public List<User> getDoctorLinkUsers(Long currentUserId) {
        return filterSelectableUsers(currentUserId, user ->
                user != null
                        && user.getRoleSet() != null
                        && user.getRoleSet().stream()
                        .filter(role -> role != null && role.getCode() != null)
                        .anyMatch(role -> "ROLE_DOCTOR".equalsIgnoreCase(role.getCode().trim()))
                        && (user.getDoctor() == null
                        || (currentUserId != null && currentUserId.equals(user.getId()))));
    }

    @Override
    public List<User> getEmployeeLinkUsers(Long currentUserId) {
        return filterSelectableUsers(currentUserId, user ->
                user.getRoleSet() != null
                        && !user.getRoleSet().isEmpty()
                        && user.getRoleSet().stream()
                        .filter(role -> role != null && role.getCode() != null)
                        .noneMatch(role -> {
                            String code = role.getCode().trim().toUpperCase(Locale.ROOT);
                            return "ROLE_DOCTOR".equals(code)
                                    || "DOCTOR".equals(code)
                                    || "ROLE_PATIENT".equals(code)
                                    || "PATIENT".equals(code)
                                    || "ROLE_ADMIN".equals(code)
                                    || "ADMIN".equals(code);
                        })
                        && (user.getEmployee() == null
                        || (currentUserId != null && currentUserId.equals(user.getId()))));
    }

    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu tài khoản không hợp lệ");
        }

        String roleCode = normalizeRoleCode(request.getRoleCode());
        Role role = this.roleRepo.findByCode(roleCode);
        if (role == null || Boolean.FALSE.equals(role.getActive())) {
            throw new IllegalArgumentException("Vai trò không hợp lệ");
        }

        String fullName = normalizeFullName(request.getFullName());
        validateFullName(fullName);

        String email = normalizeNullableEmail(request.getEmail());
        String phone = normalizeNullablePhone(request.getPhone());

        if (email != null) {
            validateEmail(email);
        }
        if (phone != null) {
            validatePhone(phone);
        }

        if (email != null && userRepo.existsByEmail(email)) {
            throw new IllegalStateException("Email đã tồn tại");
        }
        if (phone != null && userRepo.existsByPhone(phone)) {
            throw new IllegalStateException("Số điện thoại đã tồn tại");
        }

        String rawPassword = generatePassword();
        String username = generateUsername(roleCode);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRoleSet(new HashSet<>(Collections.singleton(role)));
        user.setActive(true);
        user.setEnabled(true);
        user.setAccountNonLocked(true);

        Date now = new Date();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User saved = userRepo.save(user);

        AccountResponse response = new AccountResponse();
        response.setId(saved.getId());
        response.setUsername(saved.getUsername());
        response.setRawPassword(rawPassword);
        response.setRoleCode(role.getCode());
        response.setFullName(saved.getFullName());
        response.setEmail(saved.getEmail());
        response.setPhone(saved.getPhone());
        response.setActive(Boolean.TRUE.equals(saved.getActive()));
        response.setRoles(saved.getRoleSet() == null
                ? Collections.emptyList()
                : saved.getRoleSet().stream().map(Role::getCode).collect(Collectors.toList()));
        return response;
    }

    @Override
    @Transactional
    public void deactivateAccount(Long id) {
        User user = this.userRepo.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("Tài khoản không tồn tại");
        }

        user.setActive(false);
        user.setEnabled(false);
        user.setAccountNonLocked(false);
        user.setUpdatedAt(new Date());
        this.userRepo.update(user);
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

    private List<User> filterSelectableUsers(Long currentUserId, java.util.function.Predicate<User> predicate) {
        List<User> users = this.userRepo.getActiveUsers();
        List<User> selectable = new ArrayList<>();

        for (User user : users) {
            if (user == null) {
                continue;
            }

            if (currentUserId != null && currentUserId.equals(user.getId())) {
                selectable.add(user);
                continue;
            }

            if (predicate.test(user)) {
                selectable.add(user);
            }
        }

        if (currentUserId != null && selectable.stream().noneMatch(u -> currentUserId.equals(u.getId()))) {
            User currentUser = this.userRepo.findById(currentUserId);
            if (currentUser != null && Boolean.TRUE.equals(currentUser.getActive())) {
                selectable.add(0, currentUser);
            }
        }

        return selectable;
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

    private String normalizeRoleCode(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn vai trò");
        }
        return roleCode.trim().toUpperCase(Locale.ROOT);
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

    private String generateUsername(String roleCode) {
        String prefix = roleCode.replace("ROLE_", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toLowerCase(Locale.ROOT);
        if (prefix.isBlank()) {
            prefix = "user";
        }

        for (int i = 0; i < 50; i++) {
            String candidate = prefix + generateNumericSuffix(5);
            if (!this.userRepo.existsByUsername(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Không thể sinh username duy nhất");
    }

    private String generateNumericSuffix(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private String generatePassword() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(PASSWORD_CHARS[RANDOM.nextInt(PASSWORD_CHARS.length)]);
        }
        return sb.toString();
    }

}
