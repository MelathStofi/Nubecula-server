package com.melath.nubecula.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.melath.nubecula.model.ResponseObject;
import com.melath.nubecula.storage.StorageException;
import com.melath.nubecula.storage.StorageFileNotFoundException;
import com.melath.nubecula.storage.StorageProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.melath.nubecula.storage.StorageService;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.websocket.server.PathParam;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    private final String rootDirectory;

    private final String baseUrl = "http://localhost:8080";

    @Autowired
    public FileUploadController(StorageService storageService, StorageProperties storageProperties) {
        this.storageService = storageService;
        this.rootDirectory = storageProperties.getLocation();
    }

    @GetMapping("/{dir}/**")
    @ResponseBody
    public Object listUploadedFiles(@PathVariable String dir, HttpServletRequest request) {
        String fullPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        try {
            Set<String> directories = new HashSet<>();
            Set<String> files = new HashSet<>();
            String spaceInUrl = "%20";
            storageService.loadAll(fullPath).forEach(path -> {
                if (Files.isDirectory(Paths.get(rootDirectory + fullPath + "/" + path.toString()))) {
                    directories.add(
                            baseUrl + fullPath + "/" + path.getFileName().toString().replace(" ", spaceInUrl)
                    );
                }
                else {
                    files.add(
                            baseUrl + "/files" + fullPath + "/" + path.getFileName().toString().replace(" ", spaceInUrl)
                    );
                }
            });
            return ResponseObject.builder().directories(directories).files(files).build();

        } catch (StorageFileNotFoundException e) {
            handleStorageFileNotFound(e);
            return HttpStatus.NOT_FOUND;
        }

    }

    @GetMapping("/files/{dir}/**")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String dir, HttpServletRequest request) {
        int magicNumber = 7;
        String fullPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString().substring(magicNumber);
        System.out.println(fullPath + ", dir: " + dir);
        try {
            Resource file = storageService.loadAsResource(fullPath);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"").body(file);
        } catch (StorageFileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{dir}/**")
    @ResponseBody
    public HttpStatus handleFileUpload(
            @RequestParam("file") List<MultipartFile> files,
            @PathVariable String dir,
            HttpServletRequest request
    ) {
        String fullPath = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        try {
            files.forEach(file -> storageService.store(file, fullPath));
            return HttpStatus.OK;
        } catch (StorageException e) {
            return HttpStatus.INSUFFICIENT_STORAGE;
        }
    }

    @PostMapping("/create-directory/{dir}/**")
    @ResponseBody
    public HttpStatus createDirectory(
            @PathVariable String dir,
            @RequestParam( value = "dir-name") String dirname,
            HttpServletRequest request
    ) {
        String fullPath = request.getAttribute(HandlerMapping.LOOKUP_PATH).toString().substring(18);
        System.out.println(fullPath);
        System.out.println(dir + ", " + dirname);
        try {
            storageService.createDirectory(dirname, fullPath);
            return HttpStatus.OK;
        } catch (StorageException e) {
            return HttpStatus.CONFLICT;
        }
    }

    @DeleteMapping("/{dir}/**")
    @ResponseBody
    public HttpStatus delete(@PathVariable String dir, HttpServletRequest request) {
        String fullPath = request.getAttribute(HandlerMapping.LOOKUP_PATH).toString();
        if (storageService.delete(fullPath)) return HttpStatus.OK;
        else return HttpStatus.NOT_FOUND;
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
