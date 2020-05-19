package com.melath.nubecula.security.service;

import com.melath.nubecula.security.model.NubeculaUser;
import com.melath.nubecula.security.model.UserCredentials;
import com.melath.nubecula.security.model.exception.EmailAlreadyExistsException;
import com.melath.nubecula.security.model.exception.UsernameAlreadyExistsException;
import com.melath.nubecula.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserStorage {

    private final PasswordEncoder encoder;

    UserRepository userRepository;

    private final EmailSenderService emailSenderService;

    @Autowired
    public UserStorage(UserRepository userRepository, EmailSenderService emailSenderService, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.emailSenderService = emailSenderService;
        this.encoder = encoder;
    }

    public void add(NubeculaUser user) {
        userRepository.save(user);
    }

    public NubeculaUser getByName(String name) {
        return userRepository.findByUsername(name)
                .orElseThrow(() -> new UsernameNotFoundException("Username is not found"));
    }

    public boolean signUp(UserCredentials userCredentials) throws AuthenticationException {

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
        emailSenderService.sendEmail(email, username);
        return true;
    }

}

