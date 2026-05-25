package com.evercare.dtos.request;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class DoctorRequest {

    private Long departmentId;
    private String fullName;
    private String phone;
    private String email;
    private MultipartFile avatarFile;
    private String qualification;
    private String specialization;
    private String doctorType;
    private String workStatus;
    private BigDecimal baseSalary;
    private BigDecimal hourlyRate;
    private String bio;

    public DoctorRequest() {
    }

    public DoctorRequest(Long departmentId, String fullName, String phone, String email, String qualification, String specialization,
                         MultipartFile avatarFile, String doctorType, String workStatus, BigDecimal baseSalary,
                         BigDecimal hourlyRate, String bio) {
        this.departmentId = departmentId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.avatarFile = avatarFile;
        this.qualification = qualification;
        this.specialization = specialization;
        this.doctorType = doctorType;
        this.workStatus = workStatus;
        this.baseSalary = baseSalary;
        this.hourlyRate = hourlyRate;
        this.bio = bio;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public MultipartFile getAvatarFile() {
        return avatarFile;
    }

    public void setAvatarFile(MultipartFile avatarFile) {
        this.avatarFile = avatarFile;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getDoctorType() {
        return doctorType;
    }

    public void setDoctorType(String doctorType) {
        this.doctorType = doctorType;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}