package com.melath.nubecula.storage.service;

import com.melath.nubecula.user.model.entity.NubeculaUser;
import com.melath.nubecula.user.service.UserService;
import com.melath.nubecula.storage.model.entity.NubeculaFile;
import com.melath.nubecula.storage.model.exception.NoSuchNubeculaFileException;
import com.melath.nubecula.storage.model.exception.NotNubeculaDirectoryException;
import com.melath.nubecula.storage.model.exception.StorageException;
import com.melath.nubecula.storage.model.reponse.ResponseFile;
import com.melath.nubecula.storage.repository.FileRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JpaRepoFileDataService implements FileDataService {

    private final FileRepository fileRepository;

    private final StorageService storageService;

    private final CreateResponse createResponse;

    private UserService userService;

    @Autowired
    JpaRepoFileDataService(FileRepository fileRepository, StorageService storageService, CreateResponse createResponse) {
        this.fileRepository = fileRepository;
        this.storageService = storageService;
        this.createResponse = createResponse;
    }


    @Override
    public NubeculaFile store(UUID parentDirId, MultipartFile file, NubeculaUser user) {
        String filename = FilenameUtils.getBaseName(file.getOriginalFilename());
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        assert !fileRepository.existsInDirectory(filename, extension, parentDirId) : file.getOriginalFilename() + " already exists!";
        NubeculaFile parentDir = fileRepository.findById(parentDirId).orElse(null);
        if (parentDir == null) throw new NotNubeculaDirectoryException("Not a directory");
        NubeculaFile fileData = NubeculaFile.builder()
                .fileId(UUID.randomUUID())
                .filename(filename)
                .extension(extension)
                .parentDirectory(parentDir)
                .isDirectory(false)
                .createDate(LocalDateTime.now())
                .modificationDate(LocalDateTime.now())
                .type(file.getContentType())
                .size(file.getSize())
                .owner(user)
                .shared(false)
                .build();
        return fileRepository.save(fileData);
    }


    @Override
    @Transactional
    public List<ResponseFile> loadAll(UUID id, String sort, boolean desc) throws NotNubeculaDirectoryException {
        NubeculaFile file = fileRepository.findById(id).orElse(null);
        if (file == null || !file.isDirectory()) throw new NotNubeculaDirectoryException("Not a directory");
        if (!desc) return createResponse.create((fileRepository.findAllIsDirDescSortAsc(id, sort)));
        else return createResponse.create(fileRepository.findAllIsDirDescSortDesc(id, sort));
    }


    @Override
    @Transactional
    public List<ResponseFile> loadAllShared(NubeculaUser user, String sort, boolean desc) throws UsernameNotFoundException {
        if (user.getRootDirectoryId() == null) throw new UsernameNotFoundException("Username not found");
        if (!desc) return createResponse.create(fileRepository.findAllSharedAsc(user.getRootDirectoryId(), sort));
        else return createResponse.create(fileRepository.findAllSharedDesc(user.getRootDirectoryId(), sort));
    }


    @Override
    public NubeculaFile load(UUID id) {
        return fileRepository.findById(id).orElse(null);
    }


    @Override
    public Map<String, UUID> createRootDirectory(NubeculaUser user) {
        NubeculaFile root = NubeculaFile.builder()
                .filename(user.getUsername() + " root")
                .isDirectory(true)
                .createDate(LocalDateTime.now())
                .modificationDate(LocalDateTime.now())
                .type("root")
                .owner(user)
                .shared(false)
                .build();
        NubeculaFile trashBin = NubeculaFile.builder()
                .filename(user.getUsername() + " trash bin")
                .isDirectory(true)
                .createDate(LocalDateTime.now())
                .modificationDate(LocalDateTime.now())
                .type("trash bin")
                .owner(user)
                .shared(false)
                .build();
        return Map.of("root", fileRepository.save(root).getId(), "trashBin", fileRepository.save(trashBin).getId());
    }


    @Override
    public ResponseFile createDirectory(UUID parentDirId, String dirname, NubeculaUser user) {
        assert fileRepository.findByFilename(dirname).getSize() == 0;
        NubeculaFile parentDir = fileRepository.findById(parentDirId).orElse(null);
        long inSequence = fileRepository.countByFilenameAndParentDirectoryId(dirname, parentDirId);
        if (inSequence != 0L) {
            dirname = dirname + "(" + inSequence + ")";
        }
        NubeculaFile fileData = NubeculaFile.builder()
                .filename(dirname)
                .parentDirectory(parentDir)
                .isDirectory(true)
                .createDate(LocalDateTime.now())
                .modificationDate(LocalDateTime.now())
                .type("directory")
                .owner(user)
                .shared(false)
                .build();
        return createResponse.createDir(fileRepository.save(fileData));
    }


    @Override
    public void rename(UUID id, String newName) {
        NubeculaFile file = fileRepository.findById(id).orElse(null);
        assert file != null;
        NubeculaFile parentDir = file.getParentDirectory();
        long inSequence = fileRepository.countByFilenameAndParentDirectoryId(newName, parentDir.getId());
        if (inSequence != 0L) {
            newName = newName + "(" + inSequence + ")";
        }
            file.setFilename(newName);
            file.setModificationDate(LocalDateTime.now());
            fileRepository.save(file);
    }


    @Override
    public void delete(UUID id) {
        NubeculaFile virtualPath = fileRepository.findById(id).orElse(null);
        if (virtualPath == null) throw new NoSuchNubeculaFileException("ID not found");
        if (virtualPath.isDirectory()) {
            for (NubeculaFile file : virtualPath.getNubeculaFiles()) {
                if (file.isDirectory()) {
                    delete(file.getId());
                } else {
                    storageService.delete(file.getFileId().toString());
                    userService.deleteFromUserStorageSize(file.getSize());
                    fileRepository.deleteById(file.getId());
                }
            }
        } else {
            storageService.delete(virtualPath.getFileId().toString());
            userService.deleteFromUserStorageSize(virtualPath.getSize());
        }
        fileRepository.deleteById(id);
    }


    @Override
    public void toggleShare(UUID id) {
        NubeculaFile virtualPath = fileRepository.findById(id).orElse(null);
        if (virtualPath == null) throw new NoSuchNubeculaFileException("No such file or directory");
        if (virtualPath.isDirectory()) {
            for (NubeculaFile file : virtualPath.getNubeculaFiles()) {
                if (file.isDirectory()) {
                    toggleShare(file.getId());
                } else {
                    file.setShared(!file.isShared());
                }
            }
        }
        virtualPath.setShared(!virtualPath.isShared());
        fileRepository.save(virtualPath);
    }


    @Override
    public ResponseFile replace(UUID replaceableId, UUID targetDirId) {
        NubeculaFile replaceable = fileRepository.findById(replaceableId).orElse(null);
        NubeculaFile targetDir = fileRepository.findById(targetDirId).orElse(null);
        if (replaceable == null) throw new NoSuchNubeculaFileException("replaced file ID not found");
        if (targetDir == null) throw new NoSuchNubeculaFileException("Target directory ID not found");
        replaceable.setParentDirectory(targetDir);
        NubeculaFile savedFile = fileRepository.save(replaceable);
        if (replaceable.isDirectory())
        return createResponse.createDir(savedFile);
        else return createResponse.createFile(fileRepository.save(savedFile));
    }


    @Override
    public ResponseFile copy(UUID copiedId, UUID targetDirId, NubeculaUser user) {
        NubeculaFile copied = fileRepository.findById(copiedId).orElse(null);
        NubeculaFile targetDir = fileRepository.findById(targetDirId).orElse(null);
        if (copied == null) throw new NoSuchNubeculaFileException("Copied file ID not found");
        if (targetDir == null) throw new NoSuchNubeculaFileException("Target directory ID not found");
        if (copied.isDirectory()) {
            if (!userService.addToUserStorageSize(user, getSizeOfDirectory(copied))) {
                throw new StorageException("Not enough space");
            }
            NubeculaFile newDir = copyFileData(copied, targetDir);
            for (NubeculaFile file : copied.getNubeculaFiles()) {
                if (file.isDirectory()) copy(file.getId(), newDir.getId(), user);
                else {
                    String fileId = file.getFileId().toString();
                    NubeculaFile newFile = copyFileData(file, newDir);
                    storageService.copy(fileId, newFile.getFileId().toString());
                }
            }
            return createResponse.createDir(newDir);

        } else {
            if (!userService.addToUserStorageSize(user, copied.getSize())) {
                throw new StorageException("Not enough space");
            }
            String fileId = copied.getFileId().toString();
            NubeculaFile newFile = copyFileData(copied, targetDir);
            storageService.copy(fileId, newFile.getFileId().toString());
            return createResponse.createFile(newFile);
        }
    }


    private NubeculaFile copyFileData(NubeculaFile copied, NubeculaFile targetDir) {
        fileRepository.flush();
        fileRepository.detach(copied);
        copied.setId(null);
        copied.setFileId(UUID.randomUUID());
        copied.setCreateDate(LocalDateTime.now());
        copied.setModificationDate(LocalDateTime.now());
        copied.setShared(false);
        copied.setParentDirectory(targetDir);
        return fileRepository.save(copied);
    }


    private long getSizeOfDirectory(NubeculaFile directory) {
        long sum = 0L;
        for (NubeculaFile file : directory.getNubeculaFiles()) {
            if (file.isDirectory()) getSizeOfDirectory(file);
            else sum += file.getSize();
        }
        return sum;
    }


    @Override
    public int getSizeOfDirectory(UUID directoryId) {
        NubeculaFile directory = fileRepository.findById(directoryId).orElse(null);
        if (directory == null || !directory.isDirectory()) return 0;
        return (int) getSizeOfDirectory(directory);
    }


    @Override
    @Transactional
    public List<ResponseFile> search(String searched, boolean anywhere, NubeculaUser user) {
        if (anywhere)
        return createResponse.create(fileRepository.searchByFilenameAnywhere(searched, user));
        else return createResponse.create(fileRepository.searchByFilenameBeginning(searched, user));
    }


    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}
