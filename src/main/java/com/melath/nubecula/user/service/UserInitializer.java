package com.melath.nubecula.user.service;

import com.melath.nubecula.user.model.entity.NubeculaUser;
import com.melath.nubecula.user.model.entity.Role;
import com.melath.nubecula.storage.service.FileDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
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
        /*NubeculaUser admin = NubeculaUser.builder()
                .username("admin")
                .password(encoder.encode("admin"))
                .role(Role.ADMIN)
                .role(Role.USER)
                .email("ize@hoze.com")
                .registrationDate(LocalDateTime.now())
                .storage(999999999999L)
                .inStorage(0L)
                .build();
        NubeculaUser adminEntity = userService.getUserRepository().save(admin);
        Map<String, UUID> adminIds = fileDataService.createRootDirectory(adminEntity);
        adminEntity.setRootDirectoryId(adminIds.get("root"));
        adminEntity.setTrashBinId(adminIds.get("trashBin"));
        userService.add(adminEntity);

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
        NubeculaUser stofiEntity = userService.getUserRepository().save(stofi);
        Map<String, UUID> stofiIds = fileDataService.createRootDirectory(stofiEntity);
        stofiEntity.setRootDirectoryId(stofiIds.get("root"));
        stofiEntity.setTrashBinId(stofiIds.get("trashBin"));
        userService.add(stofiEntity);*/
        userService.createUser("admin", "admin", "ize@hoze.com");
        userService.createUser("Stofi", "stofi", "melath.stofi@gmail.com");
    }
}