package com.melath.nubecula.security.service;

import com.melath.nubecula.security.model.NubeculaUser;
import com.melath.nubecula.security.model.Role;
import com.melath.nubecula.storage.service.DataService;
import com.melath.nubecula.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Service
public class UserInitializer {

    private final UserStorageService userStorageService;

    private final StorageService storageService;

    private final DataService dataService;

    private final PasswordEncoder encoder;

    @Autowired
    public UserInitializer(
            UserStorageService userStorageService,
            PasswordEncoder encoder,
            StorageService storageService,
            DataService dataService
    ) {
        this.userStorageService = userStorageService;
        this.encoder = encoder;
        this.storageService = storageService;
        this.dataService = dataService;
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
                        .email("admin@codecool.com")
                        .registrationDate(LocalDateTime.now())
                        .build()
        );
        dataService.createDirectory("admin");
        storageService.createDirectory("admin");
    }
}