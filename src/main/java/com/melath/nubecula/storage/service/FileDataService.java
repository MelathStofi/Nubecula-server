package com.melath.nubecula.storage.service;

import com.melath.nubecula.storage.model.NubeculaFile;
import com.melath.nubecula.storage.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@Service
public class FileDataService {

    private final FileRepository fileRepository;

    @Autowired
    FileDataService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void store(MultipartFile file, String username) {

    }

    public Set<NubeculaFile> loadAll(UUID id) {
        return null;
    }

    public NubeculaFile load(UUID id) {
        return null;
    }

    public void createDirectory(String dirname, String username) {

    }

    public void rename(UUID id) {

    }

    public void renameToUUID(String filename) {
        NubeculaFile file = fileRepository.findByFileName(filename);
        file.setFileName(file.getId().toString());
    }

    public void delete(UUID id) {

    }
}
