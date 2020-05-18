package com.melath.nubecula.security.service;

import com.melath.nubecula.security.model.User;
import com.melath.nubecula.security.model.UserCredentials;
import com.melath.nubecula.security.model.exception.EmailAlreadyExistException;
import com.melath.nubecula.security.model.exception.UsernameAlreadyExistException;
import com.melath.nubecula.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserStorage {

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailSenderService emailSenderService;

    public void add(User user) {
        userRepository.save(user);
    }

    public User getByName(String name) {
        return userRepository.findByUsername(name)
                .orElseThrow(() -> new UsernameNotFoundException("Username is not found"));
    }

    public boolean signUp(UserCredentials userCredentials) throws AuthenticationException {

        String username = userCredentials.getUsername();
        String email = userCredentials.getEmail();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistException();
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistException();
        }
        userRepository.save(User
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

