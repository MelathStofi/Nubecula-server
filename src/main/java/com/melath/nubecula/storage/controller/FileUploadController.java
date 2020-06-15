package com.melath.nubecula.storage.controller;

import java.nio.file.Files;
import java.util.Set;
import java.util.UUID;

import com.melath.nubecula.storage.model.NubeculaFile;
import com.melath.nubecula.storage.model.exceptions.StorageException;
import com.melath.nubecula.storage.model.exceptions.StorageFileNotFoundException;
import com.melath.nubecula.storage.model.reponse.ResponseObject;
import com.melath.nubecula.storage.service.CreateResponseObject;
import com.melath.nubecula.storage.service.DataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.melath.nubecula.storage.service.StorageService;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
public class FileUploadController {

    private final StorageService storageService;

    private final CreateResponseObject createResponseObject;

    private final DataService fileDataService;

    @Autowired
    public FileUploadController(StorageService storageService, CreateResponseObject createResponseObject, DataService fileDataService) {
        this.storageService = storageService;
        this.createResponseObject = createResponseObject;
        this.fileDataService = fileDataService;
    }

    @GetMapping({"/{id}", "/"})
    public ResponseEntity<ResponseObject> listUploadedFiles(@PathVariable(required = false) UUID id, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        if (id == null) id = fileDataService.load(username).getId();
        try {
            ResponseObject object = createResponseObject.create(id);
            return ResponseEntity.ok().body(object);

        } catch (StorageFileNotFoundException e) {
            handleStorageFileNotFound(e);
            log.error(username + " couldn't get their files");
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping({"/files/{id}", "/files"})
    public ResponseEntity<Resource> serveFile(@PathVariable( required = false ) UUID id, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        if (id == null) id = fileDataService.load(username).getId();
        try {
            Resource file = storageService.loadAsResource(fileDataService.load(id).getFileName(), username);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"").body(file);
        } catch (StorageFileNotFoundException e) {
            log.error(username + " couldn't download a file");
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping({"/{id}", "/", "/files/{id}", "/files"})
    public ResponseEntity<?> handleFileUpload(
            @PathVariable( required = false ) UUID id,
            @RequestParam("files") Set<MultipartFile> files,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        if (id == null) id = fileDataService.load(username).getId();
        try {
            UUID finalId = id;
            files.forEach(file -> {
                UUID fileId = fileDataService.store(finalId, file, username);
                storageService.store(file, username, fileId);
            });
            return ResponseEntity.ok().build();
        } catch (StorageException e) {
            log.error(username + " couldn't upload file(s)");
            return ResponseEntity.status(405).body(e.getMessage());
        }
    }

    @PostMapping({"/directories/{id}", "/directories"})
    public ResponseEntity<?> createDirectory(
            @PathVariable( required = false ) UUID id,
            @RequestBody String dirname,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        if (id == null) id = fileDataService.load(username).getId();
        try {
            fileDataService.createDirectory(id, dirname, username);
            return ResponseEntity.ok().build();
        } catch (StorageException e) {
            log.error(username + " couldn't create directory");
            return ResponseEntity.status(405).body("Directory already exists");
        }
    }

    @DeleteMapping({"/{id}", "/", "/directories/{id}", "/directories", "/files/{id}", "/files"})
    public ResponseEntity<?> delete(@PathVariable( required = false ) UUID id, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        if (id == null) id = fileDataService.load(username).getId();
        if (storageService.delete(fileDataService.load(id).getFileName(), username)) {
            fileDataService.delete(id);
            return ResponseEntity.ok().build();
        }
        else {
            log.error(username + " couldn't delete a file");
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping({"/{id}", "/", "/directories/{id}", "/directories", "/files/{id}", "/files"})
    public ResponseEntity<?> rename(
                             @RequestBody String newName,
                             @PathVariable( required = false ) UUID id,
                             HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        if (id == null) id = fileDataService.load(username).getId();
        try {
            NubeculaFile file = fileDataService.load(id);
            if (!file.isDirectory()) storageService.rename(file.getFileName(), newName, username);
            fileDataService.rename(id, newName);
            return ResponseEntity.ok().build();
        } catch (StorageException e) {
            log.error(username + " couldn't rename a file");
            return ResponseEntity.status(409).build();
        }
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.status(404).body(exc);
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<?> handleFavicon() {
        return ResponseEntity.notFound().build();
    }
}
