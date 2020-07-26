package com.melath.nubecula.user.controller;

import com.melath.nubecula.user.model.exception.NoSuchUserException;
import com.melath.nubecula.user.model.request.RequestUser;
import com.melath.nubecula.user.service.JwtTokenServices;
import com.melath.nubecula.user.service.UserService;
import com.melath.nubecula.user.model.response.ResponseUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final JwtTokenServices jwtTokenServices;

    private final UserService userService;

    @Autowired
    public UserController(
            JwtTokenServices jwtTokenServices,
            UserService userService
    ) {
        this.jwtTokenServices = jwtTokenServices;
        this.userService = userService;
    }


    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUserData(
    ) {
        try {
            return ResponseEntity.ok().body(userService.getCurrentUser());
        } catch (NoSuchUserException e) {
            return ResponseEntity.badRequest().body("User not found");
        }
    }


    @GetMapping("/")
    public List<ResponseUser> listAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) boolean anywhere
    ) {
        return search != null ? userService.searchUser(search, anywhere) : userService.getAllUsers();
    }


    @GetMapping("/{username}")
    public ResponseEntity<?> getPublicUser(@PathVariable String username) {
        try {
            return ResponseEntity.ok().body(userService.getPublicUser(username));
        } catch (NoSuchUserException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/image")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file")MultipartFile image,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        try {
            return ResponseEntity.ok().body(userService.storeProfilePicture(username, image));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Cannot store image");
        }
    }


    @PutMapping("/rename")
    public ResponseEntity<?> renameUser(@RequestBody RequestUser requestUser, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        try {
            userService.renameUser(username, requestUser.getUsername());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(405).build();
        }
    }


    @PutMapping("/description")
    public ResponseEntity<?> setUserDescription(@RequestBody RequestUser requestUser, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        try {
            userService.setUserDescription(username, requestUser.getDescription());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(405).build();
        }
    }


    @PostMapping("/delete")
    public ResponseEntity<?> deleteUser(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getUserPrincipal().getName();
        jwtTokenServices.eraseCookie(response, request);
        userService.deleteUser(username);
        return ResponseEntity.ok().build();
    }
}
