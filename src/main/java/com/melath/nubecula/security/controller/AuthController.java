package com.melath.nubecula.security.controller;

import com.melath.nubecula.security.model.NubeculaUser;
import com.melath.nubecula.security.model.SignInResponseBody;
import com.melath.nubecula.security.model.UserCredentials;
import com.melath.nubecula.security.model.exception.EmailAlreadyExistsException;
import com.melath.nubecula.security.model.exception.SignOutException;
import com.melath.nubecula.security.model.exception.SignUpException;
import com.melath.nubecula.security.model.exception.UsernameAlreadyExistsException;
import com.melath.nubecula.security.service.JwtTokenServices;
import com.melath.nubecula.security.service.UserStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.FileAlreadyExistsException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${jwt.expiration.minutes:60}")
    private long cookieMaxAgeMinutes;

    @Value("${cookie.domain}")
    private String cookieDomain;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenServices jwtTokenServices;

    private final UserStorageService userStorageService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenServices jwtTokenServices,
                          UserStorageService userStorageService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenServices = jwtTokenServices;
        this.userStorageService = userStorageService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody UserCredentials userCredentials) {
        try {
            userStorageService.signUp(userCredentials);
            return ResponseEntity.ok().body(userCredentials.getUsername());
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(409).body(SignUpException.EMAIL);
        } catch (UsernameAlreadyExistsException e) {
            return ResponseEntity.status(409).body(SignUpException.USERNAME);
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponseBody> signIn(@RequestBody UserCredentials data, HttpServletResponse response) {
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

    @PutMapping("/rename")
    public ResponseEntity<?> renameUser(@RequestBody String newName, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        try {
            NubeculaUser user = userStorageService.getByName(username);
            user.setUsername(newName);
            userStorageService.getUserRepository().save(user);
            userStorageService.renameUserData(username, newName);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(405).build();
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteUser(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getUserPrincipal().getName();
        eraseCookie(response, request);
        NubeculaUser user = userStorageService.getUserRepository().findByUsername(username).orElse(null);
        assert user != null;
        userStorageService.getUserRepository().delete(user);
        userStorageService.deleteUserData(username);
        return ResponseEntity.ok().build();
    }

    private void addTokenToCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("nubecula_token", token)
                .domain(cookieDomain) // should be parameterized
                .sameSite("Strict")  // CSRF
//                .secure(true)
                .maxAge(Duration.ofMinutes(cookieMaxAgeMinutes))
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
    public ResponseEntity<?> signOut(HttpServletResponse response, HttpServletRequest request) {
        try {
            eraseCookie(response, request);
            return ResponseEntity.status(200).build();
        } catch (SignOutException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
