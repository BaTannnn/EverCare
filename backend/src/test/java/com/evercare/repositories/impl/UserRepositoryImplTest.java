package com.evercare.repositories.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.evercare.pojo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class UserRepositoryImplTest {
    private UserRepositoryImpl repository;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
        repository = spy(new UserRepositoryImpl());
        ReflectionTestUtils.setField(repository, "factory", mock(LocalSessionFactoryBean.class));
        ReflectionTestUtils.setField(repository, "passwordEncoder", encoder);
        ReflectionTestUtils.setField(repository, "env", mock(Environment.class));
    }

    @Test
    void rejectsDisabledOrLockedUser() {
        User user = activeUser("secret");
        user.setEnabled(false);
        doReturn(user).when(repository).findByUsername("user");
        assertFalse(repository.authenticate("user", "secret"));

        user.setEnabled(true);
        user.setAccountNonLocked(false);
        assertFalse(repository.authenticate("user", "secret"));
    }

    @Test
    void acceptsActiveUserWithCorrectPasswordOnly() {
        User user = activeUser("secret");
        doReturn(user).when(repository).findByUsername("user");

        assertTrue(repository.authenticate("user", "secret"));
        assertFalse(repository.authenticate("user", "wrong"));
    }

    private User activeUser(String rawPassword) {
        User user = new User();
        user.setUsername("user");
        user.setPassword(encoder.encode(rawPassword));
        user.setActive(true);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        return user;
    }
}
