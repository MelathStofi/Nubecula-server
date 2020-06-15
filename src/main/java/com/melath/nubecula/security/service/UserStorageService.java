package com.melath.nubecula.security.service;

import com.melath.nubecula.security.model.NubeculaUser;
import com.melath.nubecula.security.model.Role;
import com.melath.nubecula.security.model.UserCredentials;
import com.melath.nubecula.security.model.exception.EmailAlreadyExistsException;
import com.melath.nubecula.security.model.exception.UsernameAlreadyExistsException;
import com.melath.nubecula.security.repository.UserRepository;
import com.melath.nubecula.storage.model.NubeculaFile;
import com.melath.nubecula.storage.service.DataService;
import com.melath.nubecula.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class UserStorageService {

    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    private final EmailSenderService emailSenderService;

    private final StorageService storageService;

    private final DataService dataService;

    @Autowired
    public UserStorageService(
            UserRepository userRepository,
            EmailSenderService emailSenderService,
            PasswordEncoder encoder,
            StorageService storageService,
            DataService dataService
    ) {
        this.userRepository = userRepository;
        this.emailSenderService = emailSenderService;
        this.encoder = encoder;
        this.storageService = storageService;
        this.dataService = dataService;
    }

    public void add(NubeculaUser user) {
        userRepository.save(user);
    }

    public NubeculaUser getByName(String name) {
        return userRepository.findByUsername(name)
                .orElseThrow(() -> new UsernameNotFoundException("Username is not found"));
    }

    public boolean signUp(UserCredentials userCredentials) throws AuthenticationException, FileAlreadyExistsException {

        String username = userCredentials.getUsername();
        String email = userCredentials.getEmail();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException();
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException();
        }
        dataService.createDirectory(username);
        storageService.createDirectory(username);
        userRepository.save(NubeculaUser
                .builder()
                .username(username)
                .password(encoder.encode(userCredentials.getPassword()))
                .role(Role.USER)
                .email(email)
                .registrationDate(LocalDate.now())
                .build()
        );
        emailSenderService.sendEmail(email, username);

        return true;
    }

    public void renameUserData(String username, String newName) {
        UUID userId = dataService.load(username).getId();
        dataService.rename(userId, newName);
        storageService.rename(username, newName, username);
    }

    public void deleteUserData(String username) {
        UUID userId = dataService.load(username).getId();
        dataService.delete(userId);
        storageService.deleteAll(username);
    }

    public UserRepository getUserRepository() {
        return this.userRepository;
    }

}

