package com.melath.nubecula.storage.controller;

import com.melath.nubecula.security.service.UserStorageService;
import com.melath.nubecula.storage.model.reponse.ResponseObject;
import com.melath.nubecula.storage.model.reponse.ResponseUser;
import com.melath.nubecula.storage.service.CreateResponseObject;
import com.melath.nubecula.storage.service.FileDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.Set;


@RestController
@RequestMapping("/users")
public class UserFileController {

    private final CreateResponseObject createResponseObject;

    private final UserStorageService userStorageService;

    private final FileDataService fileDataService;

    @Autowired
    public UserFileController(
            CreateResponseObject createResponseObject,
            UserStorageService userStorageService,
            FileDataService fileDataService
    ) {
        this.createResponseObject = createResponseObject;
        this.userStorageService = userStorageService;
        this.fileDataService = fileDataService;
    }


    @GetMapping("/")
    public Set<ResponseUser> listAllUsers() {
        return userStorageService.getAllUsers();
    }


    @GetMapping("/{username}")
    public ResponseEntity<?> listSharedFiles(@PathVariable String username) {
        try {
            ResponseObject files = createResponseObject.create(fileDataService.loadAllShared(username));
            return ResponseEntity.ok(files);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

}
