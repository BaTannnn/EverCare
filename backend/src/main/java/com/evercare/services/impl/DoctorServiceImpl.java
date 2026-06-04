package com.evercare.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.evercare.dtos.request.DoctorRequest;
import com.evercare.enums.DoctorType;
import com.evercare.enums.DoctorWorkStatus;
import com.evercare.mappers.DoctorMapper;
import com.evercare.pojo.Department;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.User;
import com.evercare.repositories.DoctorRepository;
import com.evercare.repositories.UserRepository;
import com.evercare.services.DoctorService;
import com.evercare.services.UserService;
import com.evercare.utils.AuthSupport;
import com.evercare.utils.LookupSupport;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {
    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private LookupSupport lookupSupport;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthSupport authSupport;

    @Override
    public List<Doctor> getDoctors(Map<String, String> params) {
        return this.doctorRepo.getDoctors(params);
    }

    @Override
    public Doctor getDoctorById(int id) {
        return this.doctorRepo.getDoctorById(id);
    }

    @Override
    public Doctor createDoctor(DoctorRequest req) {
        validateDoctor(req);

        Department department = this.lookupSupport.requireActiveDepartment(req.getDepartmentId());
        User user = requireDoctorAccount(req.getUserId(), null);

        Doctor doctor = DoctorMapper.toEntityForCreate(req, department);
        doctor.setUserId(user);

        if (req.getAvatarFile() != null && !req.getAvatarFile().isEmpty()) {
            try {
                Map res = this.cloudinary.uploader().upload(req.getAvatarFile().getBytes(),
                        ObjectUtils.asMap("resource_type", "auto"));
                doctor.setAvatarUrl(res.get("secure_url").toString());
            } catch (IOException ex) {
                Logger.getLogger(DoctorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        this.doctorRepo.addDoctor(doctor);

        return doctor;
    }

    @Override
    public Doctor updateDoctor(int id, DoctorRequest req) {
        validateDoctor(req);

        Doctor existing = this.doctorRepo.getDoctorById(id);

        if (existing == null) {
            throw new IllegalArgumentException("Bác sĩ không tồn tại");
        }

        if (Boolean.FALSE.equals(existing.getActive())) {
            throw new IllegalArgumentException("Bác sĩ đã bị xóa hoặc ngưng hoạt động");
        }

        Department department = this.lookupSupport.requireActiveDepartment(req.getDepartmentId());
        User user = requireDoctorAccount(req.getUserId(), existing);

        DoctorMapper.updateEntity(existing, req, department);
        if (user != null) {
            existing.setUserId(user);
        }

        if (req.getAvatarFile() != null && !req.getAvatarFile().isEmpty()) {
            try {
                Map res = this.cloudinary.uploader().upload(req.getAvatarFile().getBytes(),
                        ObjectUtils.asMap("resource_type", "auto"));
                existing.setAvatarUrl(res.get("secure_url").toString());
            } catch (IOException ex) {
                Logger.getLogger(DoctorServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        this.doctorRepo.updateDoctor(existing);

        return existing;
    }

    @Override
    public void softDelete(int id) {
        Doctor existing = this.doctorRepo.getDoctorById(id);

        if (existing == null) {
            throw new IllegalArgumentException("Bác sĩ không tồn tại");
        }

        existing.setActive(false);
        existing.setWorkStatus(DoctorWorkStatus.INACTIVE.getCode());

        this.doctorRepo.updateDoctor(existing);
    }

    @Override
    public long getTotalPages(Map<String, String> params) {
        return this.doctorRepo.getTotalPages(params);
    }

    @Override
    public List<User> getSelectableUsers(Long currentUserId) {
        return this.userService.getDoctorLinkUsers(currentUserId);
    }

    private User requireDoctorAccount(Long userId, Doctor existing) {
        if (userId == null) {
            throw new IllegalArgumentException("Vui lòng chọn tài khoản bác sĩ");
        }

        User user = this.userRepo.findById(userId);
        if (user == null || Boolean.FALSE.equals(user.getActive())) {
            throw new IllegalArgumentException("Tài khoản liên kết không tồn tại hoặc đã ngưng hoạt động");
        }

        if (!this.authSupport.hasRole(user, "DOCTOR")) {
            throw new IllegalArgumentException("Tài khoản phải có vai trò DOCTOR");
        }

        Doctor linked = user.getDoctor() != null ? user.getDoctor() : this.doctorRepo.getDoctorByUserId(user.getId());
        if (linked != null && (existing == null || !existing.getId().equals(linked.getId()))) {
            throw new IllegalArgumentException("Tài khoản này đã được gắn cho một bác sĩ khác");
        }

        return user;
    }

    private void validateDoctor(DoctorRequest req) {
        if (req.getFullName() == null || req.getFullName().isBlank()) {
            throw new IllegalArgumentException("Họ tên bác sĩ không được để trống");
        }

        if (req.getFullName().trim().length() < 2) {
            throw new IllegalArgumentException("Họ tên bác sĩ phải có ít nhất 2 ký tự");
        }

        req.setFullName(req.getFullName().trim());

        if (req.getPhone() != null) {
            req.setPhone(req.getPhone().trim());
        }

        if (req.getEmail() != null) {
            req.setEmail(req.getEmail().trim());
        }

        MultipartFile file = req.getAvatarFile();

        if (file == null || file.isEmpty()) {
            return;
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File avatar phải là hình ảnh");
        }

        long maxSize = 5 * 1024 * 1024;

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File avatar không được vượt quá 5MB");
        }

        if (req.getQualification() != null) {
            req.setQualification(req.getQualification().trim());
        }

        if (req.getSpecialization() != null) {
            req.setSpecialization(req.getSpecialization().trim());
        }

        if (req.getBio() != null) {
            req.setBio(req.getBio().trim());
        }

        if (req.getBaseSalary() == null) {
            req.setBaseSalary(BigDecimal.ZERO);
        }

        if (req.getHourlyRate() == null) {
            req.setHourlyRate(BigDecimal.ZERO);
        }

        if (req.getBaseSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Lương cơ bản không được âm");
        }

        if (req.getHourlyRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Lương theo giờ không được âm");
        }

        req.setDoctorType(DoctorType.normalize(req.getDoctorType()));
        req.setWorkStatus(DoctorWorkStatus.normalize(req.getWorkStatus()));
    }
}
