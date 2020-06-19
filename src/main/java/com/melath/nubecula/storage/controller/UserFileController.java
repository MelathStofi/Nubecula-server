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
public class UserFileController {

    private final CreateResponse createResponse;

    private final UserStorageService userStorageService;

    private final FileDataService fileDataService;

    @Autowired
    public UserFileController(
            CreateResponse createResponse,
            UserStorageService userStorageService,
            FileDataService fileDataService
    ) {
        this.createResponse = createResponse;
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
            List<ResponseFile> files = createResponse.create(fileDataService.loadAllShared(username, sort, desc));
            return ResponseEntity.ok(files);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

}
