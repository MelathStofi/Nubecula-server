package com.melath.nubecula.storage.service;

import com.melath.nubecula.storage.config.StorageProperties;
import com.melath.nubecula.storage.model.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@Service
public class CreateResponseObject {

    @Value("${base.url}")
    private String baseUrl;

    private final StorageService storageService;

    private final String rootDirectory;

    @Autowired
    public CreateResponseObject(StorageService storageService, StorageProperties storageProperties) {
        this.storageService = storageService;
        this.rootDirectory = storageProperties.getLocation();
    }

    public ResponseObject create(String fullPath, String username, HttpServletRequest request) {
        Set<String> directories = new HashSet<>();
        Set<String> files = new HashSet<>();
        String spaceInUrl = "%20";
        storageService.loadAll(username + fullPath).forEach(path -> {
            if (Files.isDirectory(Paths.get(rootDirectory + "/" + username + fullPath + "/" + path.toString()))) {
                directories.add(
                        
                        baseUrl +
                                fullPath +
                                "/" +
                                path.getFileName().toString().replace(" ", spaceInUrl)
                );
            }
            else {
                files.add(
                        baseUrl +
                                "/files" +
                                fullPath +
                                "/" +
                                path.getFileName().toString().replace(" ", spaceInUrl)
                );
            }
        });
        return ResponseObject.builder().directories(directories).files(files).build();
    }
}
