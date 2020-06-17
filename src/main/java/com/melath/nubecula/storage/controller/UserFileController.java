package com.melath.nubecula.storage.controller;

import com.melath.nubecula.storage.model.NubeculaFile;
import com.melath.nubecula.storage.model.reponse.ResponseObject;
import com.melath.nubecula.storage.service.CreateResponseObject;
import com.melath.nubecula.storage.service.FileDataService;
import com.melath.nubecula.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/users")
public class UserFileController {

    private final StorageService storageService;

    private final CreateResponseObject createResponseObject;

    private final FileDataService fileDataService;

    @Autowired
    public UserFileController(
            StorageService storageService,
            CreateResponseObject createResponseObject,
            FileDataService fileDataService
    ) {
        this.storageService = storageService;
        this.createResponseObject = createResponseObject;
        this.fileDataService = fileDataService;
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
