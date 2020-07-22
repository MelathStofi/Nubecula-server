package com.melath.nubecula.storage.controller;

import com.melath.nubecula.storage.service.FileDataService;
import com.melath.nubecula.user.model.entity.NubeculaUser;
import com.melath.nubecula.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;


@RestController
@RequestMapping("/public")
public class PublicFileController {

    private final FileDataService fileDataService;

    private final UserService userService;

    @Autowired
    public PublicFileController(FileDataService fileDataService, UserService userService) {
        this.fileDataService = fileDataService;
        this.userService = userService;
    }


    @GetMapping("/{username}")
    @Transactional
    public ResponseEntity<?> listSharedFiles(
            @PathVariable String username,
            @RequestParam(required = false, defaultValue = "filename") String sort,
            @RequestParam(required = false, defaultValue = "false") boolean desc
    ) {
        try {
            NubeculaUser user = userService.getByName(username);
            return ResponseEntity.ok().body(fileDataService.loadAllShared(user, sort, desc));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

}
