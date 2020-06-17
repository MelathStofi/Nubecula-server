package com.melath.nubecula.storage.service;

import com.melath.nubecula.storage.model.NubeculaFile;
import com.melath.nubecula.storage.model.exceptions.NoSuchNubeculaFileException;
import com.melath.nubecula.storage.model.exceptions.NotNubeculaDirectoryException;
import com.melath.nubecula.storage.repository.FileRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class JpaRepoFileDataService implements FileDataService {

    private final FileRepository fileRepository;

    @Autowired
    JpaRepoFileDataService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public NubeculaFile store(UUID parentDirId, MultipartFile file, String username) {
        assert fileRepository.doesFileAlreadyExist(file.getOriginalFilename(), parentDirId) : "File already exists";
        NubeculaFile parentDir = fileRepository.findById(parentDirId).orElse(null);
        if (parentDir == null) throw new NotNubeculaDirectoryException("Not a directory");
        NubeculaFile fileData = NubeculaFile.builder()
                .fileId(UUID.randomUUID())
                .fileName(FilenameUtils.getBaseName(file.getOriginalFilename()))
                .extension(FilenameUtils.getExtension(file.getOriginalFilename()))
                .parentDirectory(parentDir)
                .isDirectory(false)
                .createDate(LocalDateTime.now())
                .type(file.getContentType())
                .size(file.getSize())
                .owner(username)
                .shared(false)
                .build();
        return fileRepository.save(fileData);
    }

    @Override
    public Set<NubeculaFile> loadAll(UUID id) throws NotNubeculaDirectoryException {
        NubeculaFile file = fileRepository.findById(id).orElse(null);
        if (file == null || !file.isDirectory()) throw new NotNubeculaDirectoryException("Not a directory");
        return fileRepository.findAllByParentDirectoryId(id);
    }

    @Override
    public NubeculaFile load(UUID id) {
        return fileRepository.findById(id).orElse(null);
    }

    @Override
    public NubeculaFile load(String username) {
        return fileRepository.findByFileName(username);
    }

    @Override
    public void createDirectory(String username) {
            NubeculaFile fileData = NubeculaFile.builder()
                    .fileName(username)
                    .isDirectory(true)
                    .createDate(LocalDateTime.now())
                    .type("directory")
                    .owner(username)
                    .shared(false)
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
                    .type("directory")
                    .owner(username)
                    .shared(false)
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
        NubeculaFile virtualPath = fileRepository.findById(id).orElse(null);
        if (virtualPath == null) throw new NoSuchNubeculaFileException("ID not found");
        fileRepository.deleteById(id);
    }

    @Override
    public Set<NubeculaFile> loadAllShared(String username) throws UsernameNotFoundException {
        NubeculaFile parentDirectory = fileRepository.findByFileName(username);
        if (parentDirectory == null) throw new UsernameNotFoundException("Username not found");
        return fileRepository.findAllShared(parentDirectory.getId());
    }

    @Override
    public void toggleShare(UUID id) {
        NubeculaFile fileToShare = fileRepository.findById(id).orElse(null);
        if (fileToShare == null) throw new NoSuchNubeculaFileException("No such file or directory");
        fileToShare.setShared(!fileToShare.isShared());
        fileRepository.save(fileToShare);
    }

}
