package com.melath.nubecula.security.service;

import com.melath.nubecula.security.model.NubeculaUser;
import com.melath.nubecula.security.model.Role;
import com.melath.nubecula.security.model.UserCredentials;
import com.melath.nubecula.security.model.exception.EmailAlreadyExistsException;
import com.melath.nubecula.security.model.exception.UsernameAlreadyExistsException;
import com.melath.nubecula.security.repository.UserRepository;
import com.melath.nubecula.storage.service.FileDataService;
import com.melath.nubecula.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserStorageService {

    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    private final EmailSenderService emailSenderService;

    private final StorageService storageService;

    private final FileDataService fileDataService;

    @Autowired
    public UserStorageService(
            UserRepository userRepository,
            EmailSenderService emailSenderService,
            PasswordEncoder encoder,
            StorageService storageService,
            FileDataService fileDataService
    ) {
        this.userRepository = userRepository;
        this.emailSenderService = emailSenderService;
        this.encoder = encoder;
        this.storageService = storageService;
        this.fileDataService = fileDataService;
    }

    public void add(NubeculaUser user) {
        userRepository.save(user);
    }

    public NubeculaUser getByName(String name) {
        return userRepository.findByUsername(name)
                .orElseThrow(() -> new UsernameNotFoundException("Username is not found"));
    }

    public void signUp(UserCredentials userCredentials) throws AuthenticationException {

        String username = userCredentials.getUsername();
        String email = userCredentials.getEmail();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException();
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException();
        }
        fileDataService.createDirectory(username);
        storageService.createDirectory(username);
        userRepository.save(NubeculaUser
                .builder()
                .username(username)
                .password(encoder.encode(userCredentials.getPassword()))
                .role(Role.USER)
                .email(email)
                .registrationDate(LocalDateTime.now())
                .build()
        );
        emailSenderService.sendEmail(email, username);
    }

    public void renameUserData(String username, String newName) {
        UUID userId = fileDataService.load(username).getId();
        fileDataService.rename(userId, newName);
    }

    public void deleteUserData(String username) {
        UUID userId = fileDataService.load(username).getId();
        fileDataService.delete(userId);
        storageService.deleteAll(username);
    }

    public UserRepository getUserRepository() {
        return this.userRepository;
    }

}

