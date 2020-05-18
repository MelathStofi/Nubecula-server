package com.melath.nubecula.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@Service
public class UserInitializer {

    @Autowired
    private AppUserStorage appUserStorage;

    @Autowired
    private PasswordEncoder encoder;

    @PostConstruct
    public void loadInitData() {
        if (appUserStorage.appUserRepository.count() == 0) {
            loadUsers();
        }
    }

    private void loadUsers() {
        appUserStorage.add(
                AppUser.builder()
                        .username("admin")
                        .password(encoder.encode("password"))
                        .role("USER")
                        .role("ADMIN")
                        .email("admin@codecool.com")
                        .registrationDate(LocalDate.now())
                        .build()
        );

        appUserStorage.add(
                AppUser.builder()
                        .username("username")
                        .password(encoder.encode("password"))
                        .role("USER")
                        .email("user@codecool.com")
                        .registrationDate(LocalDate.now())
                        .build()
        );
    }
}