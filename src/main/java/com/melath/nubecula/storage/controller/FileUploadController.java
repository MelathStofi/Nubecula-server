package com.melath.nubecula.storage.controller;

import java.util.Set;
import java.util.UUID;

import com.melath.nubecula.storage.model.exceptions.NoSuchNubeculaFileException;
import com.melath.nubecula.storage.model.exceptions.NotNubeculaDirectoryException;
import com.melath.nubecula.storage.model.exceptions.StorageException;
import com.melath.nubecula.storage.model.exceptions.StorageFileNotFoundException;
import com.melath.nubecula.storage.model.reponse.ResponseObject;
import com.melath.nubecula.storage.service.CreateResponseObject;
import com.melath.nubecula.storage.service.FileDataService;
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

    private final FileDataService fileDataService;

    @Autowired
    public FileUploadController(StorageService storageService, CreateResponseObject createResponseObject, FileDataService fileDataService) {
        this.storageService = storageService;
        this.createResponseObject = createResponseObject;
        this.fileDataService = fileDataService;
    }


    @GetMapping({"/{id}", "/"})
    public ResponseEntity<?> listUploadedFiles(@PathVariable(required = false) UUID id, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        if (id == null) id = fileDataService.load(username).getId();
        try {
            ResponseObject object = createResponseObject.create(id);
            return ResponseEntity.ok().body(object);

        } catch (StorageFileNotFoundException e) {
            handleStorageFileNotFound(e);
            log.error(username + " couldn't get their files");
            return ResponseEntity.notFound().build();
        } catch (NotNubeculaDirectoryException e) {
            return ResponseEntity.status(405).body("Method not allowed");
        }
    }


    @GetMapping("/files/{id}")
    public ResponseEntity<Resource> serveFile(@PathVariable( required = false ) UUID id, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        try {
            Resource file = storageService.loadAsResource(id.toString());
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
                try {
                    storageService.store(file, fileId);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    fileDataService.delete(finalId);
                }
            });
            return ResponseEntity.ok().build();
        } catch (StorageException e) {
            log.error(username + " couldn't upload file(s)");
            return ResponseEntity.status(405).body(e.getMessage());
        } catch (NotNubeculaDirectoryException e) {
            return ResponseEntity.status(400).body("No such directory");
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
        try {
            storageService.delete(id.toString());
            fileDataService.delete(id);
            return ResponseEntity.ok().build();
        }
        catch (NoSuchNubeculaFileException e){
            log.error(username + " couldn't delete a file");
            return ResponseEntity.badRequest().body("No such file or directory");
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
