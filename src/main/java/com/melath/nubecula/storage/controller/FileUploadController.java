package com.melath.nubecula.storage.controller;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.melath.nubecula.storage.model.ResponseObject;
import com.melath.nubecula.storage.model.exceptions.StorageException;
import com.melath.nubecula.storage.model.exceptions.StorageFileNotFoundException;
import com.melath.nubecula.storage.config.StorageProperties;
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

    private final String rootDirectory;

    @Value("${config.allowed.origins}")
    private String baseUrl;

    @Autowired
    public FileUploadController(StorageService storageService, StorageProperties storageProperties) {
        this.storageService = storageService;
        this.rootDirectory = storageProperties.getLocation();
    }

    @GetMapping("/**")
    @ResponseBody
    public ResponseEntity<ResponseObject> listUploadedFiles(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        String fullPath = username + request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        try {
            Set<String> directories = new HashSet<>();
            Set<String> files = new HashSet<>();
            String spaceInUrl = "%20";
            storageService.loadAll(fullPath).forEach(path -> {
                if (Files.isDirectory(Paths.get(rootDirectory + "/" + fullPath + "/" + path.toString()))) {
                    directories.add(
                            baseUrl +
                            "/" +
                            fullPath +
                            "/" +
                            path.getFileName().toString().replace(" ", spaceInUrl)
                    );
                }
                else {
                    files.add(
                            baseUrl +
                            "/files/" +
                            fullPath +
                            "/" +
                            path.getFileName().toString().replace(" ", spaceInUrl)
                    );
                }
            });
            return ResponseEntity.ok().body(ResponseObject.builder().directories(directories).files(files).build());

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
            @RequestParam("file") Set<MultipartFile> files,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        String fullPath = username + request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        try {
            files.forEach(file -> storageService.store(file, fullPath));
            return ResponseEntity.ok().build();
        } catch (StorageException e) {
            log.error(username + " couldn't upload file(s)");
            return ResponseEntity.status(405).build();
        }
    }

    @PostMapping("/create-directory/**")
    @ResponseBody
    public ResponseEntity<?> createDirectory(
            @RequestParam( value = "dir-name") String dirname,
            HttpServletRequest request
    ) {
        int magicNumber = 17; // EZT JAVÍTSD KI!!
        String username = request.getUserPrincipal().getName();
        String fullPath = username + request.getAttribute(HandlerMapping.LOOKUP_PATH).toString().substring(magicNumber);
        try {
            storageService.createDirectory(dirname, fullPath);
            return ResponseEntity.ok().build();
        } catch (StorageException e) {
            log.error(username + " couldn't create directory");
            return ResponseEntity.status(403).build();
        } catch (FileAlreadyExistsException e) {
            return ResponseEntity.status(409).build();
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
                             @RequestParam( value = "new-name") String newName,
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
