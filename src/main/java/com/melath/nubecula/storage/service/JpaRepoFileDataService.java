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

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

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
                .filename(FilenameUtils.getBaseName(file.getOriginalFilename()))
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
    @Transactional
    public Stream<NubeculaFile> loadAll(UUID id, String sort, boolean desc) throws NotNubeculaDirectoryException {
        NubeculaFile file = fileRepository.findById(id).orElse(null);
        if (file == null || !file.isDirectory()) throw new NotNubeculaDirectoryException("Not a directory");
        if (!desc) return fileRepository.findAllIsDirDescSortAsc(id, sort);
        else return fileRepository.findAllIsDirDescSortDesc(id, sort);
    }


    @Override
    @Transactional
    public Stream<NubeculaFile> loadAllShared(String username, String sort, boolean desc) throws UsernameNotFoundException {
        NubeculaFile parentDirectory = fileRepository.findByFilename(username);
        if (parentDirectory == null) throw new UsernameNotFoundException("Username not found");
        if (!desc) return fileRepository.findAllSharedAsc(parentDirectory.getId(), sort);
        else return fileRepository.findAllSharedDesc(parentDirectory.getId(), sort);
    }


    @Override
    public NubeculaFile load(UUID id) {
        return fileRepository.findById(id).orElse(null);
    }


    @Override
    public NubeculaFile load(String username) {
        return fileRepository.findByFilename(username);
    }


    @Override
    public void createDirectory(String username) {
            NubeculaFile fileData = NubeculaFile.builder()
                    .filename(username)
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
        assert fileRepository.findByFilename(dirname).getSize() == 0;
        fileRepository.findById(parentDirId).ifPresent(parentDir -> {
            NubeculaFile fileData = NubeculaFile.builder()
                    .filename(dirname)
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
            file.setFilename(newName);
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
    public void toggleShare(UUID id) {
        NubeculaFile fileToShare = fileRepository.findById(id).orElse(null);
        if (fileToShare == null) throw new NoSuchNubeculaFileException("No such file or directory");
        fileToShare.setShared(!fileToShare.isShared());
        fileRepository.save(fileToShare);
    }


}
