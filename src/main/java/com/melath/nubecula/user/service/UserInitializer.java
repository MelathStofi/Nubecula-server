package com.melath.nubecula.user.service;

import com.melath.nubecula.user.model.entity.NubeculaUser;
import com.melath.nubecula.user.model.entity.Role;
import com.melath.nubecula.storage.service.FileDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class UserInitializer {

    private final UserService userService;

    private final FileDataService fileDataService;

    private final PasswordEncoder encoder;

    @Autowired
    public UserInitializer(
            UserService userService,
            PasswordEncoder encoder,
            FileDataService fileDataService
    ) {
        this.userService = userService;
        this.encoder = encoder;
        this.fileDataService = fileDataService;
    }


    @PostConstruct
    public void loadInitData() {
        if (userService.getUserRepository().count() == 0) {
            loadUsers();
        }
    }


    private void loadUsers() {
        NubeculaUser admin = NubeculaUser.builder()
                .username("admin")
                .password(encoder.encode("admin"))
                .role(Role.ADMIN)
                .role(Role.USER)
                .email("ize@hoze.com")
                .registrationDate(LocalDateTime.now())
                .storage(999999999999L)
                .inStorage(0L)
                .build();
        userService.add(admin);
        Map<String, UUID> adminIds = fileDataService.createRootDirectory(admin);
        admin.setRootDirectoryId(adminIds.get("root"));
        admin.setTrashBinId(adminIds.get("trashBin"));
        userService.add(admin);

        NubeculaUser stofi = NubeculaUser.builder()
                .username("Stofi")
                .password(encoder.encode("stofi"))
                .role(Role.USER)
                .email("melath.stofi@gmail.com")
                .registrationDate(LocalDateTime.now())
                .storage(3000000L)
                .inStorage(0L)
                .friend(admin)
                .build();
        userService.add(stofi);
        Map<String, UUID> stofiIds = fileDataService.createRootDirectory(admin);
        stofi.setRootDirectoryId(stofiIds.get("root"));
        stofi.setTrashBinId(stofiIds.get("trashBin"));
        userService.add(stofi);
    }
}