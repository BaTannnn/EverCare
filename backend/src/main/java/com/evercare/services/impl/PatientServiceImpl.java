package com.evercare.services.impl;

import com.evercare.dtos.request.PatientRequest;
import com.evercare.dtos.response.PatientResponse;
import com.evercare.mappers.PatientMapper;
import com.evercare.pojo.Patient;
import com.evercare.pojo.User;
import com.evercare.repositories.PatientRepository;
import com.evercare.repositories.UserRepository;
import com.evercare.services.PatientService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {
    private static final Set<String> BLOOD_TYPES = new HashSet<>(Set.of(
            "A", "A+", "A-", "B", "B+", "B-", "AB", "AB+", "AB-", "O", "O+", "O-"
    ));

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Override
    public PatientResponse createProfile(PatientRequest request) {
        User currentUser = getCurrentUser();
        requireActiveUser(currentUser);

        if (currentUser.getPatient() != null) {
            throw new IllegalStateException("User này đã có hồ sơ bệnh nhân");
        }
        if (patientRepository.existsActiveByUserId(currentUser.getId())) {
            throw new IllegalStateException("User này đã có hồ sơ bệnh nhân");
        }

        Patient patient = new Patient();
        applyBaseUserFields(patient, currentUser);
        applyMedicalFields(patient, request, false);
        patient.setActive(true);
        patient.setCreatedAt(new Date());
        patient.setUpdatedAt(new Date());
        applyPatientCode(patient);

        Patient saved = patientRepository.save(patient);
        return PatientMapper.toResponse(saved);
    }

    @Override
    public PatientResponse getMyProfile() {
        Patient patient = requirePatientProfile();
        return PatientMapper.toResponse(patient);
    }

    @Override
    public PatientResponse updateMyProfile(PatientRequest request) {
        User currentUser = getCurrentUser();
        requireActiveUser(currentUser);
        Patient patient = requirePatientProfile();

        applyMedicalFields(patient, request, true);
        syncUserFromPatientRequest(currentUser, request);
        patient.setUpdatedAt(new Date());

        userRepository.update(currentUser);
        Patient updated = patientRepository.update(patient);
        return PatientMapper.toResponse(updated);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new SecurityException("Vui lòng đăng nhập");
        }

        User user = userRepository.findByUsername(authentication.getName());
        if (user == null) {
            throw new SecurityException("Vui lòng đăng nhập");
        }
        return user;
    }

    private Patient getCurrentPatientOrThrow() {
        User currentUser = getCurrentUser();
        requireActiveUser(currentUser);
        Patient patient = patientRepository.getPatientByUserId(currentUser.getId());
        if (patient == null || !Boolean.TRUE.equals(patient.getActive())) {
            throw new java.util.NoSuchElementException("Bạn chưa tạo hồ sơ bệnh nhân");
        }
        return patient;
    }

    private Patient requirePatientProfile() {
        return getCurrentPatientOrThrow();
    }

    private void requireActiveUser(User user) {
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new SecurityException("Tài khoản hiện tại không hoạt động");
        }
    }

    private void applyBaseUserFields(Patient patient, User user) {
        patient.setFullName(user.getFullName());
        patient.setPhone(user.getPhone());
        patient.setEmail(user.getEmail());
        patient.setUserId(user);
    }

    private void applyMedicalFields(Patient patient, PatientRequest request, boolean allowPartialUpdate) {
        if (request == null) {
            if (allowPartialUpdate) {
                return;
            }
            throw new IllegalArgumentException("Dữ liệu hồ sơ bệnh nhân không hợp lệ");
        }

        String gender = normalizeOptional(request.getGender(), "gender");
        if (gender != null) {
            validateGender(gender);
            patient.setGender(gender);
        } else if (!allowPartialUpdate) {
            throw new IllegalArgumentException("Gender là bắt buộc");
        }

        String dateOfBirthValue = normalizeOptional(request.getDateOfBirth(), "dateOfBirth");
        if (dateOfBirthValue != null) {
            Date dob = parseDate(dateOfBirthValue);
            if (dob.after(new Date())) {
                throw new IllegalArgumentException("dateOfBirth không được ở tương lai");
            }
            patient.setDateOfBirth(dob);
        } else if (!allowPartialUpdate) {
            throw new IllegalArgumentException("dateOfBirth là bắt buộc");
        }

        String citizenId = normalizeOptional(request.getCitizenId(), "citizenId");
        if (citizenId != null) {
            if (!citizenId.equals(patient.getCitizenId()) && patientRepository.existsByCitizenId(citizenId)) {
                throw new IllegalStateException("Citizen ID đã tồn tại");
            }
            patient.setCitizenId(citizenId);
        }

        String healthInsuranceNo = normalizeOptional(request.getHealthInsuranceNo(), "healthInsuranceNo");
        if (healthInsuranceNo != null) {
            if (!healthInsuranceNo.equals(patient.getHealthInsuranceNo())
                    && patientRepository.existsByHealthInsuranceNo(healthInsuranceNo)) {
                throw new IllegalStateException("Health insurance number đã tồn tại");
            }
            patient.setHealthInsuranceNo(healthInsuranceNo);
        }

        String address = normalizeNullableText(request.getAddress());
        if (address != null) {
            patient.setAddress(address);
        }

        String emergencyContactName = normalizeNullableText(request.getEmergencyContactName());
        if (emergencyContactName != null) {
            patient.setEmergencyContactName(emergencyContactName);
        }

        String emergencyContactPhone = normalizeOptional(request.getEmergencyContactPhone(), "emergencyContactPhone");
        if (emergencyContactPhone != null) {
            validatePhone(emergencyContactPhone, "emergencyContactPhone");
            patient.setEmergencyContactPhone(emergencyContactPhone);
        }

        String bloodType = normalizeOptional(request.getBloodType(), "bloodType");
        if (bloodType != null) {
            validateBloodType(bloodType);
            patient.setBloodType(bloodType.toUpperCase(Locale.ROOT));
        }

        if (request.getAllergyNote() != null) {
            patient.setAllergyNote(request.getAllergyNote().trim());
        }

        if (request.getMedicalHistoryNote() != null) {
            patient.setMedicalHistoryNote(request.getMedicalHistoryNote().trim());
        }
    }

    private void syncUserFromPatientRequest(User user, PatientRequest request) {
        if (request == null) {
            return;
        }
        if (request.getFullName() != null && !request.getFullName().trim().isBlank()) {
            user.setFullName(request.getFullName().trim());
        } else if (request.getFullName() != null) {
            throw new IllegalArgumentException("fullName không được rỗng");
        }

        if (request.getPhone() != null && !request.getPhone().trim().isBlank()) {
            validatePhone(request.getPhone().trim(), "phone");
            user.setPhone(request.getPhone().trim());
        } else if (request.getPhone() != null) {
            throw new IllegalArgumentException("phone không được rỗng");
        }

        if (request.getEmail() != null && !request.getEmail().trim().isBlank()) {
            String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
            validateEmail(email);
            user.setEmail(email);
        } else if (request.getEmail() != null) {
            throw new IllegalArgumentException("email không được rỗng");
        }
    }

    private void applyPatientCode(Patient patient) {
        if (patient.getPatientCode() == null || patient.getPatientCode().isBlank()) {
            String ts = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
            patient.setPatientCode("PAT_" + ts + "_" + suffix);
        }
    }

    private String normalizeOptional(String value, String fieldName) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " không được rỗng");
        }
        return trimmed;
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
    }

    private void validatePhone(String phone, String fieldName) {
        if (phone == null || phone.isBlank() || !phone.matches("^\\d{9,15}$")) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ");
        }
    }

    private void validateGender(String gender) {
        String upper = gender.toUpperCase(Locale.ROOT);
        if (!Set.of("MALE", "FEMALE", "OTHER").contains(upper)) {
            throw new IllegalArgumentException("Gender không hợp lệ");
        }
    }

    private void validateBloodType(String bloodType) {
        if (!BLOOD_TYPES.contains(bloodType.toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Blood type không hợp lệ");
        }
    }

    private Date parseDate(String value) {
        try {
            LocalDate localDate = LocalDate.parse(value);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception ex) {
            throw new IllegalArgumentException("dateOfBirth không hợp lệ");
        }
    }
}
