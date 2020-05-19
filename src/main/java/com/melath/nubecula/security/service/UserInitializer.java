package com.melath.nubecula.security.service;

import com.melath.nubecula.security.model.NubeculaUser;
import com.melath.nubecula.security.service.UserStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@Service
public class UserInitializer {

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private PasswordEncoder encoder;

    @PostConstruct
    public void loadInitData() {
        if (userStorage.userRepository.count() == 0) {
            loadUsers();
        }
    }

    private void loadUsers() {
        userStorage.add(
                NubeculaUser.builder()
                        .username("admin")
                        .password(encoder.encode("password"))
                        .role("USER")
                        .role("ADMIN")
                        .email("admin@codecool.com")
                        .registrationDate(LocalDate.now())
                        .build()
        );

        userStorage.add(
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