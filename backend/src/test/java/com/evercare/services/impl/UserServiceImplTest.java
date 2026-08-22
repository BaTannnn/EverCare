package com.evercare.services.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.cloudinary.Cloudinary;
import com.evercare.pojo.Role;
import com.evercare.pojo.User;
import com.evercare.repositories.RoleRepository;
import com.evercare.repositories.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private Cloudinary cloudinary;
    @Mock private RoleRepository roleRepository;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl();
        ReflectionTestUtils.setField(service, "userRepo", userRepository);
        ReflectionTestUtils.setField(service, "cloudinary", cloudinary);
        ReflectionTestUtils.setField(service, "passwordEncoder", new BCryptPasswordEncoder());
        ReflectionTestUtils.setField(service, "roleRepo", roleRepository);
    }

    @Test
    void mapsRoleAuthoritiesAndAccountFlags() {
        User user = new User();
        user.setUsername("doctor");
        user.setPassword("encoded");
        user.setActive(true);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        Role role = new Role();
        role.setCode("ROLE_DOCTOR");
        user.setRoleSet(Set.of(role));
        when(userRepository.findByUsername("doctor")).thenReturn(user);

        UserDetails details = service.loadUserByUsername("doctor");

        assertTrue(details.isEnabled());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> "ROLE_DOCTOR".equals(a.getAuthority())));
    }

    @Test
    void disabledDomainUserProducesDisabledUserDetails() {
        User user = new User();
        user.setUsername("disabled");
        user.setPassword("encoded");
        user.setActive(false);
        user.setEnabled(false);
        user.setAccountNonLocked(true);
        when(userRepository.findByUsername("disabled")).thenReturn(user);

        UserDetails details = service.loadUserByUsername("disabled");

        assertFalse(details.isEnabled());
    }
}
