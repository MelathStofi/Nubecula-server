package com.melath.nubecula.storage.controller;

import com.melath.nubecula.storage.service.FileDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.UUID;


@RestController
@RequestMapping("/public")
public class PublicFileController {

    private final FileDataService fileDataService;

    @Autowired
    public PublicFileController(FileDataService fileDataService) {
        this.fileDataService = fileDataService;
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
            return ResponseEntity.ok().body(search != null
                    ? fileDataService.searchShared(username, search, anywhere)
                    : fileDataService.loadAllShared(username, id, sort, desc));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

}