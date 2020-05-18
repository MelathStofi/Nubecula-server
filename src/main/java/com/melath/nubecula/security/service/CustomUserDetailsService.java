package com.melath.nubecula.security.service;

import com.melath.nubecula.security.model.User;
import com.melath.nubecula.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository users;

    /**
     * Loads the user from the DB and converts it to Spring Security's internal User object
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username: " + username + " not found"));

        return new User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles()
                        .stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(Collectors.toList()));
    }
}
