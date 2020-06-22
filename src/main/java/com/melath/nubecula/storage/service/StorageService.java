package com.melath.nubecula.storage.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

public interface StorageService {

    void init();

    void store(MultipartFile file, UUID fileId);

    Set<Path> loadAll(String dirName);

    Path load(String filename);

    Resource loadAsResource(String filename);

    boolean delete(String filename);

    void deleteAll(String username);

    void rename(String filename, String newName);

    void createDirectory(String name);

    void copy(String filename, String newFilename);

}
