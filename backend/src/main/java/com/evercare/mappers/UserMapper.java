package com.evercare.mappers;

import com.evercare.dtos.response.UserRegisterResponse;
import com.evercare.pojo.Role;
import com.evercare.pojo.User;
import java.util.Collections;
import java.util.stream.Collectors;

public class UserMapper {

    private UserMapper() {
    }

    public static UserRegisterResponse toResponse(User user, boolean hasPatientProfile) {
        UserRegisterResponse response = new UserRegisterResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setFullName(user.getFullName());
        response.setAvatarFile(user.getAvatarUrl());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setRoles(user.getRoleSet() == null
                ? Collections.emptyList()
                : user.getRoleSet().stream().map(Role::getCode).collect(Collectors.toList()));
        boolean activePatient = user.getPatient() != null && Boolean.TRUE.equals(user.getPatient().getActive());
        response.setPatientId(activePatient ? user.getPatient().getId() : null);
        response.setEnabled(Boolean.TRUE.equals(user.getEnabled()));
        response.setActive(Boolean.TRUE.equals(user.getActive()));
        response.setHasPatientProfile(activePatient);
        return response;
    }
}
