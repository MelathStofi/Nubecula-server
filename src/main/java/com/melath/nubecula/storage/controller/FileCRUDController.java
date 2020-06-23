package com.melath.nubecula.storage.controller;

import java.util.Set;
import java.util.UUID;

import com.melath.nubecula.security.service.UserStorageService;
import com.melath.nubecula.storage.model.NubeculaFile;
import com.melath.nubecula.storage.model.exceptions.NoSuchNubeculaFileException;
import com.melath.nubecula.storage.model.exceptions.NotNubeculaDirectoryException;
import com.melath.nubecula.storage.model.exceptions.StorageException;
import com.melath.nubecula.storage.model.exceptions.StorageFileNotFoundException;
import com.melath.nubecula.storage.service.CreateResponse;
import com.melath.nubecula.storage.service.FileDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.melath.nubecula.storage.service.StorageService;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

@RestController
@Slf4j
public class FileCRUDController {

    private final StorageService storageService;

    private final CreateResponse createResponse;

    private final FileDataService fileDataService;

    private final UserStorageService userStorageService;

    @Autowired
    public FileCRUDController(
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

    // RETRIEVE
    @GetMapping({"/{id}", "/"})
    @Transactional
    public ResponseEntity<?> listUploadedFiles(
            @PathVariable(required = false) UUID id,
            @RequestParam(required = false, defaultValue = "filename") String sort,
            @RequestParam(required = false, defaultValue = "false") boolean desc,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        if (id == null) id = fileDataService.load(username).getId();
        try {
            return ResponseEntity.ok().body(createResponse.create(fileDataService.loadAll(id, sort, desc)));

        } catch (StorageFileNotFoundException e) {
            handleStorageFileNotFound(e);
            log.error(username + " couldn't get " + id);
            return ResponseEntity.notFound().build();
        } catch (NotNubeculaDirectoryException e) {
            return ResponseEntity.status(405).body("ID: " + id + " not found");
        }
    }

    // RETRIEVE
    @GetMapping("/files/{id}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable( required = false ) UUID id,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        try {
            String fileId = fileDataService.load(id).getFileId().toString();
            Resource file = storageService.loadAsResource(fileId);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"").body(file);
        } catch (StorageFileNotFoundException e) {
            log.error(username + " couldn't download " + id);
            return ResponseEntity.notFound().build();
        }
    }

    // CREATE
    @PostMapping({"/{id}", "/", "/files/{id}", "/files"})
    public ResponseEntity<?> handleFileUpload(
            @PathVariable( required = false ) UUID id,
            @RequestParam("files") Set<MultipartFile> files,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        try {
            if (id == null) id = fileDataService.load(username).getId();
            if (!userStorageService.addToUserStorageSize(username, files)) throw new StorageException("Not enough space");
            final UUID finalId = id;
            files.forEach(file -> {
                NubeculaFile savedFile = fileDataService.store(finalId, file, username);
                try {
                    storageService.store(file, savedFile.getFileId());
                } catch (Exception e) {
                    log.error(e.getMessage());
                    fileDataService.delete(savedFile.getId());
                }
            });
            return ResponseEntity.ok().build();
        } catch (StorageException e) {
            log.error(username + " couldn't upload file(s) due to space deficit");
            return ResponseEntity.status(507).body(e.getMessage());
        } catch (NotNubeculaDirectoryException e) {
            return ResponseEntity.status(400).body("No such directory");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body("Get the fuck out!");
        }
    }

    // CREATE
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


    // UPDATE
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

    // UPDATE
    @PutMapping({"/toggle-share/{id}", "/directories/toggle-share/{id}", "/files/toggle-share/{id}"})
    public ResponseEntity<?> toggleShare(@PathVariable UUID id) {
        try {
            fileDataService.toggleShare(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchNubeculaFileException e) {
            return ResponseEntity.badRequest().body("ID: " + id + " not found");
        }
    }

    // DELETE
    @DeleteMapping({"/{id}", "/", "/directories/{id}", "/directories", "/files/{id}", "/files"})
    public ResponseEntity<?> delete(
            @PathVariable( required = false ) UUID id,
            HttpServletRequest request
    ) {
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

    // UPDATE
    @PutMapping({"/replace/{id}", "/directories/replace/{id}", "/files/replace{id}"})
    public ResponseEntity<?> replace(
            @PathVariable UUID id,
            @RequestBody( required = false) UUID targetDirId,
            HttpServletRequest request
    ) {
        if (targetDirId == null) targetDirId = fileDataService.load(request.getUserPrincipal().getName()).getId();
        try {
            fileDataService.replace(id, targetDirId);
            return ResponseEntity.ok().build();
        } catch (NoSuchNubeculaFileException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // CREATE
    @PutMapping({"/copy/{id}", "/directories/copy/{id}", "/files/copy{id}"})
    public ResponseEntity<?> copy(
            @PathVariable UUID id,
            @RequestBody( required = false ) UUID targetDirId,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        if (targetDirId == null) targetDirId = fileDataService.load(username).getId();
        try {
            fileDataService.copy(id, targetDirId, username);
            return ResponseEntity.ok().build();
        } catch (NoSuchNubeculaFileException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (StorageException e) {
            return ResponseEntity.status(507).body(e.getMessage());
        }
    }


    // EXCEPTION
    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.status(404).body(exc);
    }


    @GetMapping("/favicon.ico")
    public ResponseEntity<?> handleFavicon() {
        return ResponseEntity.notFound().build();
    }
}
