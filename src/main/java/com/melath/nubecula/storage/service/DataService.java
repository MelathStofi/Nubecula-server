package com.melath.nubecula.storage.service;

import com.melath.nubecula.storage.model.NubeculaFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

public interface DataService {
    UUID store(UUID parentDirId, MultipartFile file, String username);

    Set<NubeculaFile> loadAll(UUID id);

    NubeculaFile load(UUID id);

    NubeculaFile load(String filename);

    void createDirectory(UUID parentDirId, String dirname, String username);

    void createDirectory(String username);

    void rename(UUID id, String newName);

    void delete(UUID id);

}
