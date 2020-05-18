package com.melath.nubecula.security.controller;

import com.melath.nubecula.security.model.SignInResponseBody;
import com.melath.nubecula.security.model.UserCredentials;
import com.melath.nubecula.security.model.exception.EmailAlreadyExistException;
import com.melath.nubecula.security.model.exception.SignOutException;
import com.melath.nubecula.security.model.exception.SignUpException;
import com.melath.nubecula.security.model.exception.UsernameAlreadyExistException;
import com.melath.nubecula.security.service.JwtTokenServices;
import com.melath.nubecula.security.service.UserStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${jwt.expiration.minutes:60}")
    private long cookieMaxAgeMinutes;

    @Value("${cookie.domain}")
    private String cookiedomain;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenServices jwtTokenServices;

    @Autowired
    UserStorage userStorage;

    @PostMapping("/sign-up")
    public ResponseEntity signUp(@RequestBody UserCredentials userCredentials) {
        try {
            userStorage.signUp(userCredentials);
            return ResponseEntity.ok().body(userCredentials.getUsername());
        } catch (EmailAlreadyExistException e) {
            return ResponseEntity.status(409).body(SignUpException.EMAIL);
        } catch (UsernameAlreadyExistException e) {
            return ResponseEntity.status(409).body(SignUpException.USERNAME);
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity signIn(@RequestBody UserCredentials data, HttpServletResponse response) {
        try {
            String username = data.getUsername();
            // authenticationManager.authenticate calls loadUserByUsername in CustomUserDetailsService
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
            List<String> roles = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            String token = jwtTokenServices.generateToken(authentication);
            addTokenToCookie(response, token);
            SignInResponseBody signInBody = new SignInResponseBody(username, roles);
            return ResponseEntity.ok().body(signInBody);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(403).build();
        }
    }

    private void addTokenToCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .domain(cookiedomain) // should be parameterized
                .sameSite("Strict")  // CSRF
//                .secure(true)
                .maxAge(Duration.ofHours(cookieMaxAgeMinutes / 60))
                .httpOnly(true)      // XSS
                .path("/")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void eraseCookie(HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            cookie.setPath("/");
            cookie.setMaxAge(0);
            cookie.setValue("");
            response.addCookie(cookie);
        }
    }

    @PostMapping("/sign-out")
    public ResponseEntity signOut(HttpServletResponse response, HttpServletRequest request) {
        try {
            eraseCookie(response, request);
            return ResponseEntity.status(200).build();
        } catch (SignOutException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
