package com.melath.nubecula.security.service;

import com.melath.nubecula.security.model.NubeculaUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@Service
public class UserInitializer {

    private final UserStorageService userStorageService;

    private final PasswordEncoder encoder;

    @Autowired
    public UserInitializer(UserStorageService userStorageService, PasswordEncoder encoder) {
        this.userStorageService = userStorageService;
        this.encoder = encoder;
    }

    @PostConstruct
    public void loadInitData() {
        if (userStorageService.getUserRepository().count() == 0) {
            loadUsers();
        }
    }

    private void loadUsers() {
        userStorageService.add(
                NubeculaUser.builder()
                        .username("admin")
                        .password(encoder.encode("password"))
                        .role("USER")
                        .role("ADMIN")
                        .email("admin@codecool.com")
                        .registrationDate(LocalDate.now())
                        .build()
        );

        userStorageService.add(
                NubeculaUser.builder()
                        .username("username")
                        .password(encoder.encode("password"))
                        .role("USER")
                        .email("user@codecool.com")
                        .registrationDate(LocalDate.now())
                        .build()
        );
    }
}