package com.melath.nubecula.security.service;

import com.melath.nubecula.security.model.NubeculaUser;
import com.melath.nubecula.security.model.UserCredentials;
import com.melath.nubecula.security.model.exception.EmailAlreadyExistsException;
import com.melath.nubecula.security.model.exception.UsernameAlreadyExistsException;
import com.melath.nubecula.security.repository.UserRepository;
import com.melath.nubecula.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDate;

@Service
public class UserStorageService {

    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    private final EmailSenderService emailSenderService;

    private final StorageService storageService;

    @Autowired
    public UserStorageService(
            UserRepository userRepository,
            EmailSenderService emailSenderService,
            PasswordEncoder encoder,
            StorageService storageService
    ) {
        this.userRepository = userRepository;
        this.emailSenderService = emailSenderService;
        this.encoder = encoder;
        this.storageService = storageService;
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
        userRepository.save(NubeculaUser
                .builder()
                .username(username)
                .password(encoder.encode(userCredentials.getPassword()))
                .role("USER")
                .email(email)
                .registrationDate(LocalDate.now())
                .build()
        );

        storageService.createDirectory(username);

        emailSenderService.sendEmail(email, username);
        return true;
    }

    public UserRepository getUserRepository() {
        return this.userRepository;
    }

}

