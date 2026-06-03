package com.evercare.services.impl;

import com.evercare.dtos.request.PatientRequest;
import com.evercare.dtos.response.PatientResponse;
import com.evercare.enums.BloodType;
import com.evercare.mappers.PatientMapper;
import com.evercare.pojo.Patient;
import com.evercare.pojo.User;
import com.evercare.repositories.PatientRepository;
import com.evercare.services.PatientService;
import com.evercare.utils.AuthSupport;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AuthSupport authSupport;

    @Override
    public PatientResponse createProfile(PatientRequest request) {
        User currentUser = this.authSupport.getCurrentUserAllowInactive();
        requireActiveUser(currentUser);

        if (patientRepository.getPatientByUserId(currentUser.getId()) != null) {
            throw new IllegalStateException("User này đã có hồ sơ bệnh nhân");
        }

        Patient patient = new Patient();
        patient.setUserId(currentUser);
        applyPatientRequest(patient, request);
        patient.setActive(true);
        patient.setCreatedAt(new Date());
        patient.setUpdatedAt(new Date());
        applyPatientCode(patient);

        Patient saved = patientRepository.save(patient);
        return PatientMapper.toResponse(saved);
    }

    @Override
    public PatientResponse getMyProfile() {
        User currentUser = this.authSupport.getCurrentUserAllowInactive();
        requireActiveUser(currentUser);
        Patient patient = this.authSupport.requireCurrentPatient(currentUser, "Bạn chưa tạo hồ sơ bệnh nhân");
        return PatientMapper.toResponse(patient);
    }

    @Override
    public PatientResponse updateMyProfile(PatientRequest request) {
        User currentUser = this.authSupport.getCurrentUserAllowInactive();
        requireActiveUser(currentUser);
        Patient patient = this.authSupport.requireCurrentPatient(currentUser, "Bạn chưa tạo hồ sơ bệnh nhân");

        applyPatientRequest(patient, request);
        patient.setUpdatedAt(new Date());

        Patient updated = patientRepository.update(patient);
        return PatientMapper.toResponse(updated);
    }

    @Override
    public List<PatientResponse> searchForReceptionist(String keyword, Integer limit) {
        int normalizedLimit = limit == null ? 10 : limit;
        return this.patientRepository.searchPatientsByKeyword(keyword, normalizedLimit)
                .stream()
                .map(PatientMapper::toResponse)
                .toList();
    }

    private void requireActiveUser(User user) {
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new SecurityException("Tài khoản hiện tại không hoạt động");
        }
    }

    private void applyPatientRequest(Patient patient, PatientRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu hồ sơ bệnh nhân không hợp lệ");
        }

        String fullName = normalizeRequiredText(request.getFullName(), "fullName");
        String phone = normalizeRequiredText(request.getPhone(), "phone");
        String gender = normalizeRequiredText(request.getGender(), "gender");
        String dateOfBirthValue = normalizeRequiredText(request.getDateOfBirth(), "dateOfBirth");
        String citizenId = normalizeRequiredText(request.getCitizenId(), "citizenId");

        validateGender(gender);
        validatePhone(phone, "phone");

        Date dob = parseDate(dateOfBirthValue);
        if (dob.after(new Date())) {
            throw new IllegalArgumentException("dateOfBirth không được ở tương lai");
        }

        if (!citizenId.equals(patient.getCitizenId()) && patientRepository.existsByCitizenId(citizenId)) {
            throw new IllegalStateException("Citizen ID đã tồn tại");
        }

        String healthInsuranceNo = normalizeNullableText(request.getHealthInsuranceNo());
        if (healthInsuranceNo != null
                && !healthInsuranceNo.equals(patient.getHealthInsuranceNo())
                && patientRepository.existsByHealthInsuranceNo(healthInsuranceNo)) {
            throw new IllegalStateException("Health insurance number đã tồn tại");
        }

        String email = normalizeOptionalEmail(request.getEmail());
        String emergencyContactName = normalizeNullableText(request.getEmergencyContactName());
        String emergencyContactPhone = normalizeNullableText(request.getEmergencyContactPhone());
        String bloodType = normalizeNullableText(request.getBloodType());
        String allergyNote = normalizeNullableText(request.getAllergyNote());
        String medicalHistoryNote = normalizeNullableText(request.getMedicalHistoryNote());

        patient.setFullName(fullName);
        patient.setPhone(phone);
        patient.setGender(gender.toUpperCase(Locale.ROOT));
        patient.setDateOfBirth(dob);
        patient.setCitizenId(citizenId);
        patient.setHealthInsuranceNo(healthInsuranceNo);
        patient.setAddress(normalizeNullableText(request.getAddress()));
        patient.setEmergencyContactName(emergencyContactName);
        patient.setEmergencyContactPhone(emergencyContactPhone);
        patient.setBloodType(normalizeBloodType(bloodType));
        patient.setAllergyNote(allergyNote);
        patient.setMedicalHistoryNote(medicalHistoryNote);
        patient.setEmail(email);
    }

    private void applyPatientCode(Patient patient) {
        if (patient.getPatientCode() == null || patient.getPatientCode().isBlank()) {
            String ts = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
            patient.setPatientCode("PAT_" + ts + "_" + suffix);
        }
    }

    private String normalizeRequiredText(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " là bắt buộc");
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

    private String normalizeOptionalEmail(String email) {
        String normalized = normalizeNullableText(email);
        if (normalized == null) {
            return null;
        }
        if (!normalized.contains("@") || !normalized.contains(".")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
        return normalized.toLowerCase(Locale.ROOT);
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
        BloodType.normalize(bloodType);
    }

    private String normalizeBloodType(String bloodType) {
        if (bloodType == null) {
            return null;
        }
        validateBloodType(bloodType);
        return bloodType.toUpperCase(Locale.ROOT);
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
