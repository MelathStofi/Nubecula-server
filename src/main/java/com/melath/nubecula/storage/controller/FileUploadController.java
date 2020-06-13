package com.melath.nubecula.storage.controller;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.melath.nubecula.storage.model.NubeculaFile;
import com.melath.nubecula.storage.model.ResponseObject;
import com.melath.nubecula.storage.model.exceptions.StorageException;
import com.melath.nubecula.storage.model.exceptions.StorageFileNotFoundException;
import com.melath.nubecula.storage.config.StorageProperties;
import com.melath.nubecula.storage.repository.FileRepository;
import com.melath.nubecula.storage.service.CreateResponseObject;
import com.melath.nubecula.storage.service.FileDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.melath.nubecula.storage.service.StorageService;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
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

    @GetMapping("/**")
    @ResponseBody
    public ResponseEntity<ResponseObject> listUploadedFiles(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        String fullPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        if (fullPath.length() <= 1) fullPath = "";
        try {
            ResponseObject object = createResponseObject.create(fullPath, username, request);
            return ResponseEntity.ok().body(object);

        } catch (StorageFileNotFoundException e) {
            handleStorageFileNotFound(e);
            log.error(username + " couldn't get their files");
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/files/**")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(HttpServletRequest request) {
        int magicNumber = 6; // EZT JAVÍTSD KI!!
        String username = request.getUserPrincipal().getName();
        String fullPath = username + request.getAttribute(HandlerMapping
                .PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
                .toString()
                .substring(magicNumber);
        try {
            Resource file = storageService.loadAsResource(fullPath);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"").body(file);
        } catch (StorageFileNotFoundException e) {
            log.error(username + " couldn't download a file");
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/**")
    @ResponseBody
    public ResponseEntity<?> handleFileUpload(
            @RequestParam("files") Set<MultipartFile> files,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        String fullPath = username + request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        try {
            files.forEach(file -> {
                fileDataService.store(file, username);
                storageService.store(file, fullPath);
            });
            return ResponseEntity.ok().build();
        } catch (StorageException e) {
            log.error(username + " couldn't upload file(s)");
            return ResponseEntity.status(405).body(e.getMessage());
        }
    }

    @PostMapping("/directories/**")
    @ResponseBody
    public ResponseEntity<?> createDirectory(
            @RequestBody String dirname,
            HttpServletRequest request
    ) {
        int magicNumber = 12; // EZT JAVÍTSD KI!!
        String username = request.getUserPrincipal().getName();
        String fullPath = username + request.getAttribute(HandlerMapping.LOOKUP_PATH).toString().substring(magicNumber);
        try {
            fileDataService.createDirectory(dirname, username);
            return ResponseEntity.ok().build();
        } catch (StorageException e) {
            log.error(username + " couldn't create directory");
            return ResponseEntity.status(403).build();
        }
    }

    @DeleteMapping("/**")
    @ResponseBody
    public ResponseEntity<?> delete(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        String fullPath = username + request.getAttribute(HandlerMapping.LOOKUP_PATH).toString();
        if (storageService.delete(fullPath)) return ResponseEntity.ok().build();
        else {
            log.error(username + " couldn't delete a file");
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/**")
    @ResponseBody
    public ResponseEntity<?> rename(
                             @RequestBody String newName,
                             HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        String fullPath = username + request.getAttribute(HandlerMapping.LOOKUP_PATH).toString();
        try {
            storageService.rename(newName, fullPath);
            return ResponseEntity.ok().build();
        } catch (StorageException e) {
            log.error(username + " couldn't rename a file");
            return ResponseEntity.status(409).build();
        }
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<?> handleFavicon() {
        return ResponseEntity.notFound().build();
    }
}
