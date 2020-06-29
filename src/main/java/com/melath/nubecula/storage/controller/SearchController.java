package com.melath.nubecula.storage.controller;

import com.melath.nubecula.security.service.UserStorageService;
import com.melath.nubecula.storage.service.CreateResponse;
import com.melath.nubecula.storage.service.FileDataService;
import com.melath.nubecula.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
