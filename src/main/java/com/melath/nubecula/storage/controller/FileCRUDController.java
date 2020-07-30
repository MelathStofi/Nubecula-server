package com.melath.nubecula.storage.controller;

import java.util.UUID;

import com.melath.nubecula.storage.model.exception.*;
import com.melath.nubecula.storage.model.entity.NubeculaFile;
import com.melath.nubecula.storage.model.request.RequestAction;
import com.melath.nubecula.storage.model.request.RequestFile;
import com.melath.nubecula.util.ResponseCreator;
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

    private final FileDataService fileDataService;

    private final ResponseCreator responseCreator;

    @Autowired
    public FileCRUDController(
            StorageService storageService,
            FileDataService fileDataService,
            ResponseCreator responseCreator
    ) {
        this.storageService = storageService;
        this.fileDataService = fileDataService;
        this.responseCreator = responseCreator;
    }


    // RETRIEVE
    @GetMapping({"/{id}", "/"})
    @Transactional
    public ResponseEntity<?> listAll(
            @PathVariable(required = false) UUID id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) boolean anywhere,
            @RequestParam(required = false, defaultValue = "filename") String sort,
            @RequestParam(required = false, defaultValue = "false") boolean desc,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        try {
            return ResponseEntity.ok().body(
                    search != null
                            ? fileDataService.search(username, search, anywhere)
                            : fileDataService.loadAll(username, id, sort, desc)
            );
        } catch (StorageFileNotFoundException e) {
            handleStorageFileNotFound(e);
            log.error(username + ": RETRIEVE "+ id +" failed");
            return ResponseEntity.notFound().build();
        } catch (NotNubeculaDirectoryException e) {
            return ResponseEntity.status(405).body("ID: " + id + " not found");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    //RETRIEVE
    @GetMapping({"/directories", "/directories/{id}"})
    @Transactional
    public ResponseEntity<?> listAllDirectories(
            @PathVariable(required = false) UUID id,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        try {
            return ResponseEntity.ok().body(fileDataService.loadAllDirectories(username, id));
        } catch (NotNubeculaDirectoryException e) {
            return ResponseEntity.badRequest().body("ID: " + id + " not found");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // RETRIEVE
    @CrossOrigin
    @GetMapping("/files/{id}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        try {
            String fileId = fileDataService.load(id).getFileId().toString();
            Resource file = storageService.loadAsResource(fileId);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"").body(file);
        } catch (StorageFileNotFoundException e) {
            log.error(username + ": DOWNLOAD "+ id +" failed");
            return ResponseEntity.notFound().build();
        }
    }


    // CREATE
    @PostMapping({"/{id}", "/", "/files/{id}", "/files"})
    public ResponseEntity<?> handleFileUpload(
            @PathVariable( required = false ) UUID id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        try {
            NubeculaFile savedFile = fileDataService.store(username, id, file);
            try {
                storageService.store(file, savedFile.getFileId());
            } catch (Exception e) {
                log.error(e.getMessage());
                fileDataService.delete(savedFile.getId());
            }
            return ResponseEntity.ok().body(responseCreator.createFile(savedFile));
        } catch (StorageException e) {
            log.error("FATAL: " + e.getMessage());
            return ResponseEntity.status(507).body(
                    "The drive is full!\nPlease send a message to the admin to notice them" +
                            "\nSend \"Error: 507\" to this email: mealath.stofi@gmail.com"
            );
        } catch (NotNubeculaDirectoryException e) {
            return ResponseEntity.status(400).body("No such directory");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UserStorageException e) {
            return ResponseEntity.badRequest().body("OUT OF STORAGE");
        }
    }


    //RETRIEVE
    @GetMapping({"/directories/directory", "/directories/directory/{id}"})
    public ResponseEntity<?> getDirectory(
            @PathVariable(required = false) UUID id,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        try {
            return ResponseEntity.ok().body(fileDataService.loadDirectory(username, id));
        } catch (NoSuchNubeculaFileException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // CREATE
    @PostMapping({"/directories/{id}", "/directories"})
    public ResponseEntity<?> createDirectory(
            @PathVariable( required = false ) UUID id,
            @RequestBody RequestFile directory,
            HttpServletRequest request
    ) {
        String dirname = directory.getName();
        String username = request.getUserPrincipal().getName();
        try {
            return ResponseEntity.ok().body(fileDataService.createDirectory(username, id, dirname));
        } catch (StorageException e) {
            log.error(username + ": CREATE_DIR in "+ id +" failed");
            return ResponseEntity.status(405).body("Directory already exists");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // UPDATE
    @PutMapping({"/{id}", "/", "/directories/{id}", "/directories", "/files/{id}", "/files"})
    public ResponseEntity<?> rename(
            @RequestBody RequestFile directory,
            @PathVariable( required = false ) UUID id,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        try {
            fileDataService.rename(id, directory.getName());
            return ResponseEntity.ok().build();
        } catch (StorageException e) {
            log.error(username + ": RENAME "+ id +" failed");
            return ResponseEntity.status(409).build();
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
    @DeleteMapping({"/{id}", "/directories/{id}", "/files{id}"})
    public ResponseEntity<?> delete(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        try {
            fileDataService.delete(id);
            return ResponseEntity.ok().build();
        }
        catch (NoSuchNubeculaFileException e){
            log.error(request.getUserPrincipal().getName() + ": DELETE "+ id +" failed" );
            return ResponseEntity.badRequest().body("No such file or directory");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // RETRIEVE
    @GetMapping("/trash-bin")
    @Transactional
    public ResponseEntity<?> listTrashBin(
            @RequestParam(required = false, defaultValue = "filename") String sort,
            @RequestParam(required = false, defaultValue = "false") boolean desc,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();

        try {
            return ResponseEntity.ok().body(fileDataService.loadTrashBin(username, sort, desc));
        } catch (NoSuchNubeculaFileException | UsernameNotFoundException e) {
            log.error(username + ": RETRIEVE TRASH_BIN failed");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // UPDATE
    @PutMapping("/trash-bin")
    public ResponseEntity<?> moveToTrashBin(
            @RequestBody RequestAction requestAction,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        try {
            return ResponseEntity.ok().body(fileDataService.moveToTrashBin(username, requestAction.getFiles()));
        } catch (NoSuchNubeculaFileException | UsernameNotFoundException e) {
            log.error(username + ": REPLACE "+ requestAction.getFiles().toString() + " to trash bin failed");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // UPDATE
    @PutMapping({"/replace", "/directories/replace", "/files/replace"})
    public ResponseEntity<?> replace(
            @RequestBody RequestAction requestAction,
            HttpServletRequest request
    ) {
        UUID targetDirId = requestAction.getTargetDirId();
        String username = request.getUserPrincipal().getName();
        try {
            return ResponseEntity.ok().body(fileDataService.replace(username, requestAction.getFiles(), targetDirId));
        } catch (NoSuchNubeculaFileException | UsernameNotFoundException e) {
            log.error(username + ": REPLACE "+ requestAction.getFiles().toString() + " to " + targetDirId +" failed");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // CREATE
    @PostMapping({"/copy", "/directories/copy", "/files/copy"})
    public ResponseEntity<?> copy(
            @RequestBody RequestAction requestAction,
            HttpServletRequest request
    ) {
        String username = request.getUserPrincipal().getName();
        UUID targetDirId = requestAction.getTargetDirId();
        try {
            return ResponseEntity.ok().body(fileDataService.copy(username, requestAction.getFiles(), targetDirId));
        } catch (NoSuchNubeculaFileException | UsernameNotFoundException e) {
            log.error(username + ": COPY "+ requestAction.getFiles().toString() + " to " + targetDirId + " failed");
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (StorageException e) {
            log.error("FATAL: " + e.getMessage());
            return ResponseEntity.status(507).body(
                    "The drive is full!\nPlease send a message to the admin to notice them" +
                            "\nSend \"Error: 507\" to this email: mealath.stofi@gmail.com"
            );
        }
    }


    //RETRIEVE
    @GetMapping("/size/{id}")
    public ResponseEntity<?> getSize(@PathVariable UUID id) {
            NubeculaFile file = fileDataService.load(id);
            return ResponseEntity.ok().body(
                    file.isDirectory() ? fileDataService.getSizeOfDirectory(id) : file.getSize()
            );
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
