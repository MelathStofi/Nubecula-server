package com.melath.nubecula.storage.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

public interface StorageService {

    void init();

    void store(MultipartFile file, String username, UUID fileId);

    Set<Path> loadAll(String dirName, String username);

    Path load(String filename, String username);

    Resource loadAsResource(String filename, String username);

    boolean delete(String filename, String username);

    void deleteAll(String username);

    void rename(String filename, String newName, String username);

    void createDirectory(String name);

}
