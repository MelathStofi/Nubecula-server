package com.melath.nubecula.storage.service;

import com.melath.nubecula.storage.model.NubeculaFile;
import com.melath.nubecula.storage.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class FileDataService implements DataService{

    private final FileRepository fileRepository;

    @Autowired
    FileDataService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public UUID store(UUID parentDirId, MultipartFile file, String username) {
        assert fileRepository.doesFileAlreadyExist(file.getOriginalFilename(), parentDirId) : "File already exists";
        fileRepository.findById(parentDirId).ifPresent(parentDir -> {
            NubeculaFile fileData = NubeculaFile.builder()
                    .fileName(file.getResource().getFilename())
                    .extension(getExtension(file.getOriginalFilename()).orElse(""))
                    .parentDirectory(parentDir)
                    .isDirectory(false)
                    .createDate(LocalDateTime.now())
                    .type(file.getContentType())
                    .size(file.getSize())
                    .owner(username)
                    .build();
            fileRepository.save(fileData);
        });
        return fileRepository.findByFileName(file.getName()).getId();
    }

    @Override
    public Set<NubeculaFile> loadAll(UUID id) {
        return fileRepository.findAllByParentDirectoryId(id);
    }

    @Override
    public NubeculaFile load(UUID id) {
        return fileRepository.findById(id).orElse(null);
    }

    @Override
    public NubeculaFile load(String filename) {
        return fileRepository.findByFileName(filename);
    }

    @Override
    public void createDirectory(String username) {
            NubeculaFile fileData = NubeculaFile.builder()
                    .fileName(username)
                    .isDirectory(true)
                    .createDate(LocalDateTime.now())
                    .owner(username)
                    .build();
            fileRepository.save(fileData);
    }

    @Override
    public void createDirectory(UUID parentDirId, String dirname, String username) {
        assert fileRepository.findByFileName(dirname).getSize() == 0;
        fileRepository.findById(parentDirId).ifPresent(parentDir -> {
            NubeculaFile fileData = NubeculaFile.builder()
                    .fileName(dirname)
                    .parentDirectory(parentDir)
                    .isDirectory(true)
                    .createDate(LocalDateTime.now())
                    .owner(username)
                    .build();
            fileRepository.save(fileData);
        });
    }

    @Override
    public void rename(UUID id, String newName) {
        fileRepository.findById(id).ifPresent(file -> {
            file.setFileName(newName);
            fileRepository.save(file);
        });
    }

    @Override
    public void delete(UUID id) {
        fileRepository.deleteById(id);
    }

    private Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }
}
