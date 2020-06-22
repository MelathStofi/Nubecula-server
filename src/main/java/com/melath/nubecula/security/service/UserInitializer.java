package com.melath.nubecula.security.service;

import com.melath.nubecula.security.model.NubeculaUser;
import com.melath.nubecula.security.model.Role;
import com.melath.nubecula.storage.service.FileDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Service
public class UserInitializer {

    private final UserStorageService userStorageService;

    private final FileDataService fileDataService;

    private final PasswordEncoder encoder;

    @Autowired
    public UserInitializer(
            UserStorageService userStorageService,
            PasswordEncoder encoder,
            FileDataService fileDataService
    ) {
        this.userStorageService = userStorageService;
        this.encoder = encoder;
        this.fileDataService = fileDataService;
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
                        .role(Role.ADMIN)
                        .role(Role.USER)
                        .email("ize@hoze.com")
                        .registrationDate(LocalDateTime.now())
                        .storage(999999999999999999L)
                        .inStorage(-999999999999999999L)
                        .build()
        );
        fileDataService.createDirectory("admin");

        userStorageService.add(
                NubeculaUser.builder()
                        .username("Stofi")
                        .password(encoder.encode("stofasz"))
                        .role(Role.USER)
                        .email("melath.stofi@gmail.com")
                        .registrationDate(LocalDateTime.now())
                        .storage(3000000L)
                        .inStorage(0L)
                        .build()
        );
        fileDataService.createDirectory("Stofi");
    }
}