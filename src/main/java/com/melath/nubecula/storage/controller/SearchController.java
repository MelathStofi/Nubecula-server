package com.melath.nubecula.storage.controller;

import com.melath.nubecula.security.model.NubeculaUser;
import com.melath.nubecula.security.model.exception.NoSuchUserException;
import com.melath.nubecula.security.service.UserStorageService;
import com.melath.nubecula.storage.service.CreateResponse;
import com.melath.nubecula.storage.service.FileDataService;
import com.melath.nubecula.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class SearchController {

    private final StorageService storageService;

    private final CreateResponse createResponse;

    private final FileDataService fileDataService;

    private final UserStorageService userStorageService;

    @Autowired
    public SearchController(
            StorageService storageService,
            CreateResponse createResponse,
            FileDataService fileDataService,
            UserStorageService userStorageService
    ) {
        this.storageService = storageService;
        this.createResponse = createResponse;
        this.fileDataService = fileDataService;
        this.userStorageService = userStorageService;
    }


    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam String search,
            HttpServletRequest request
    ) {
        NubeculaUser user = userStorageService.getByName(request.getUserPrincipal().getName());
        try {
            if (user == null) {
                throw new NoSuchUserException("No such user");
            }
            return ResponseEntity.ok().body(fileDataService.search(search, user.getUsername()));
        } catch (NoSuchUserException e) {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

}
