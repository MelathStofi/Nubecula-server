package com.melath.nubecula.storage.controller;

import com.melath.nubecula.security.service.UserStorageService;
import com.melath.nubecula.storage.model.reponse.ResponseFile;
import com.melath.nubecula.storage.model.reponse.ResponseUser;
import com.melath.nubecula.storage.service.CreateResponse;
import com.melath.nubecula.storage.service.FileDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserStorageService userStorageService;

    private final FileDataService fileDataService;

    @Autowired
    public UserController(
            UserStorageService userStorageService,
            FileDataService fileDataService
    ) {
        this.userStorageService = userStorageService;
        this.fileDataService = fileDataService;
    }


    @GetMapping("/")
    public List<ResponseUser> listAllUsers() {
        return userStorageService.getAllUsers();
    }


    @GetMapping("/{username}")
    @Transactional
    public ResponseEntity<?> listSharedFiles(
            @PathVariable String username,
            @RequestParam(required = false, defaultValue = "filename") String sort,
            @RequestParam(required = false, defaultValue = "false") boolean desc
    ) {
        try {
            return ResponseEntity.ok(fileDataService.loadAllShared(username, sort, desc));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

}
