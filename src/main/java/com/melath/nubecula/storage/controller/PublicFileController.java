package com.melath.nubecula.storage.controller;

import com.melath.nubecula.storage.model.exception.NoSuchNubeculaFileException;
import com.melath.nubecula.storage.model.exception.StorageFileNotFoundException;
import com.melath.nubecula.storage.service.FileDataService;
import com.melath.nubecula.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.UUID;


@RestController
@RequestMapping("/public")
@Slf4j
public class PublicFileController {

    private final FileDataService fileDataService;

    private final StorageService storageService;

    @Autowired
    public PublicFileController(
            FileDataService fileDataService,
            StorageService storageService
    ) {
        this.fileDataService = fileDataService;
        this.storageService = storageService;
    }


    @GetMapping({"/{username}/{id}", "/{username}"})
    @Transactional
    public ResponseEntity<?> listSharedFiles(
            @PathVariable String username,
            @PathVariable(required = false) UUID id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) boolean anywhere,
            @RequestParam(required = false, defaultValue = "filename") String sort,
            @RequestParam(required = false, defaultValue = "false") boolean desc
    ) {
        try {
            if (username == null) throw new UsernameNotFoundException("Username not found");
            return ResponseEntity.ok().body(search != null
                    ? fileDataService.searchShared(username, search, anywhere)
                    : fileDataService.loadAllShared(username, id, sort, desc));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping({"/{username}/directories/directory", "/{username}/directories/directory/{id}"})
    public ResponseEntity<?> getDirectory(
            @PathVariable String username,
            @PathVariable(required = false) UUID id
    ) {
        try {
            if (username == null) throw new UsernameNotFoundException("Username not found");
            return ResponseEntity.ok().body(fileDataService.loadDirectory(username, id));
        } catch (NoSuchNubeculaFileException | UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{username}/files/{id}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String username,
            @PathVariable UUID id
    ) {

        try {
            String fileId = fileDataService.loadShared(id).getFileId().toString();
            Resource file = storageService.loadAsResource(fileId);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"").body(file);
        } catch (StorageFileNotFoundException e) {
            log.error("public: DOWNLOAD "+ username + "'s file " + id +" failed");
            return ResponseEntity.notFound().build();
        } catch (NoSuchNubeculaFileException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
