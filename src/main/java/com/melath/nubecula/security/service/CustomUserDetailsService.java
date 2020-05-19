package com.melath.nubecula.security.service;


import com.melath.nubecula.security.model.NubeculaUser;
import com.melath.nubecula.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository users;

    @Autowired
    public CustomUserDetailsService(UserRepository users) {
        this.users = users;
    }

    /**
     * Loads the user from the DB and converts it to Spring Security's internal User object
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        NubeculaUser nubeculaUser = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username: " + username + " not found"));

        return new User(
                nubeculaUser.getUsername(),
                nubeculaUser.getPassword(),
                nubeculaUser.getRoles()
                        .stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(Collectors.toList()));
    }
}
