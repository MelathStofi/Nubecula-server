package com.melath.nubecula.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Set;

public interface StorageService {

    void init();

    void store(MultipartFile file, String dir);

    Set<Path> loadAll(String dir);

    Path load(String filename);

    Resource loadAsResource(String filename);

    void createDirectory(String dirName, String dir);

    boolean delete(String location);

    void deleteAll();

    void rename(String newName, String location);

}
