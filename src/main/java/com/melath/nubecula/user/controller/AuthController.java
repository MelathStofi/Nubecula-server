package com.melath.nubecula.user.controller;

import com.melath.nubecula.user.model.exception.EmailAlreadyExistsException;
import com.melath.nubecula.user.model.exception.SignOutException;
import com.melath.nubecula.user.model.exception.SignUpException;
import com.melath.nubecula.user.model.exception.UsernameAlreadyExistsException;
import com.melath.nubecula.user.model.request.UserCredentials;
import com.melath.nubecula.user.model.response.UserResponseBody;
import com.melath.nubecula.user.service.JwtTokenServices;
import com.melath.nubecula.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenServices jwtTokenServices;

    private final UserService userService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenServices jwtTokenServices,
                          UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenServices = jwtTokenServices;
        this.userService = userService;
    }


    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody UserCredentials userCredentials) {
        try {
            userService.signUp(userCredentials);
            return ResponseEntity.ok().body(userCredentials.getUsername());
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(409).body(SignUpException.EMAIL);
        } catch (UsernameAlreadyExistsException e) {
            return ResponseEntity.status(409).body(SignUpException.USERNAME);
        }
    }


    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseBody> signIn(@RequestBody UserCredentials data, HttpServletResponse response) {
        try {
            String username = data.getUsername();
            // authenticationManager.authenticate calls loadUserByUsername in CustomUserDetailsService
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
            List<String> roles = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            String token = jwtTokenServices.generateToken(authentication);
            jwtTokenServices.addTokenToCookie(response, token);
            UserResponseBody signInBody = new UserResponseBody(username, roles);
            return ResponseEntity.ok().body(signInBody);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(403).build();
        }
    }


    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut(HttpServletResponse response, HttpServletRequest request) {
        try {
            jwtTokenServices.eraseCookie(response, request);
            return ResponseEntity.status(200).build();
        } catch (SignOutException e) {
            return ResponseEntity.status(403).build();
        }
    }


    @GetMapping("/verify")
    public ResponseEntity<?> confirmAuth(HttpServletRequest request) {
        try {
            return ResponseEntity.ok().body(request.getUserPrincipal().getName());
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
}
