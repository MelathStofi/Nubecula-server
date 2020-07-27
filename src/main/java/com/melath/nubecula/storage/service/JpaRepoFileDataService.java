package com.melath.nubecula.storage.service;

import com.melath.nubecula.storage.model.exception.UserStorageException;
import com.melath.nubecula.user.model.entity.NubeculaUser;
import com.melath.nubecula.user.service.UserService;
import com.melath.nubecula.storage.model.entity.NubeculaFile;
import com.melath.nubecula.storage.model.exception.NoSuchNubeculaFileException;
import com.melath.nubecula.storage.model.exception.NotNubeculaDirectoryException;
import com.melath.nubecula.storage.model.exception.StorageException;
import com.melath.nubecula.storage.model.reponse.ResponseFile;
import com.melath.nubecula.storage.repository.FileRepository;
import com.melath.nubecula.util.ResponseCreator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class JpaRepoFileDataService implements FileDataService {

    private final FileRepository fileRepository;

    private final StorageService storageService;

    private final ResponseCreator responseCreator;

    private UserService userService;

    @Autowired
    JpaRepoFileDataService(
            FileRepository fileRepository,
            StorageService storageService,
            ResponseCreator responseCreator
    ) {
        this.fileRepository = fileRepository;
        this.storageService = storageService;
        this.responseCreator = responseCreator;
    }


    /* <------------------------------------------- CREATE -------------------------------------------> */

    @Override
    public NubeculaFile store(String username, UUID parentDirId, MultipartFile file) {
        if (!userService.addToUserStorageSize(username, file)) throw new UserStorageException("Not enough space");
        NubeculaUser user = userService.getByName(username);
        NubeculaFile parentDir = getOperableDir(username, parentDirId);
        String filename = FilenameUtils.getBaseName(file.getOriginalFilename());
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        assert !fileRepository.existsInDirectory(filename, extension, parentDirId)
                : file.getOriginalFilename() + " already exists!";
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
    public void createDirectory(String dirname, NubeculaUser user) {
        NubeculaFile newDir = NubeculaFile.builder()
                .filename(user.getUsername() + " " + dirname)
                .isDirectory(true)
                .createDate(LocalDateTime.now())
                .modificationDate(LocalDateTime.now())
                .type(dirname)
                .owner(user)
                .shared(false)
                .build();
        fileRepository.save(newDir);
    }


    @Override
    public ResponseFile createDirectory(String username, UUID parentDirId, String dirname) {
        NubeculaUser user = userService.getByName(username);
        NubeculaFile parentDir = getOperableDir(username, parentDirId);
        assert fileRepository.findByFilename(dirname).getSize() == 0;
        long inSequence = fileRepository.countByFilenameAndParentDirectoryId(dirname, parentDir.getId());
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
        return responseCreator.createDir(fileRepository.save(fileData));
    }


    @Override
    public ResponseFile[] copy(String username, List<ResponseFile> copiedFiles, UUID targetDirId) {
        NubeculaFile targetDir = getOperableDir(username, targetDirId);
        int size = copiedFiles.size();
        ResponseFile[] responseFiles = new ResponseFile[size];
        for (int i = 0; i < size; i++) {
            responseFiles[i] = copyOne(username, copiedFiles.get(i).getId(), targetDir);
        }
        return responseFiles;

    }


    private ResponseFile copyOne(String username, UUID copiedId, NubeculaFile targetDir) {
        NubeculaFile copied = load(copiedId);
        if (copied.isDirectory()) {
            if (!userService.addToUserStorageSize(username, getSizeOfDirectory(copied))) {
                throw new StorageException("Not enough space");
            }
            NubeculaFile newDir = cloneFileEntity(copied, targetDir);
            for (NubeculaFile file : copied.getNubeculaFiles()) {
                if (file.isDirectory()) copyOne(username, file.getId(), newDir);
                else {
                    String fileId = file.getFileId().toString();
                    NubeculaFile newFile = cloneFileEntity(file, newDir);
                    storageService.copy(fileId, newFile.getFileId().toString());
                }
            }
            return responseCreator.createDir(newDir);

        } else {
            if (!userService.addToUserStorageSize(username, copied.getSize())) {
                throw new StorageException("Not enough space");
            }
            String fileId = copied.getFileId().toString();
            NubeculaFile newFile = cloneFileEntity(copied, targetDir);
            storageService.copy(fileId, newFile.getFileId().toString());
            return responseCreator.createFile(newFile);
        }
    }


    private NubeculaFile cloneFileEntity(NubeculaFile copied, NubeculaFile targetDir) {
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


    /* <------------------------------------------- RETRIEVE -------------------------------------------> */

    @Override
    public NubeculaFile load(UUID id) {
        NubeculaFile file = fileRepository.findById(id).orElse(null);
        if (file == null) throw new NoSuchNubeculaFileException("There is no file with id " + id);
        return file;
    }


    @Override
    @Transactional
    public List<ResponseFile> loadTrashBin(String username, String sort, boolean desc) {
        NubeculaFile trashBin = getTrashBin(username);
        return responseCreator.create(fileRepository.findAllByParentDirectoryId(trashBin.getId(), getSort(sort, desc)));
    }


    public NubeculaFile getRoot(String username) {
        return fileRepository.findFirstByOwnerUsernameAndType(username, "root directory");
    }


    public NubeculaFile getTrashBin(String username) {
        return fileRepository.findFirstByOwnerUsernameAndType(username, "trash bin");
    }


    private NubeculaFile getOperableDir(String username, UUID id) {
        if (id == null) return getRoot(username);
        else return load(id);
    }


    @Override
    public ResponseFile loadDirectory(String username, UUID id) throws NoSuchNubeculaFileException {
        return responseCreator.createDir(id != null ? load(id) : getRoot(username));
    }


    @Override
    public ResponseFile loadSharedDirectory(String username, UUID id) throws NoSuchNubeculaFileException {
        NubeculaFile requestedDirectory = fileRepository.findByIdAndOwnerUsernameAndSharedIsTrue(id, username);
        if (requestedDirectory == null) throw new NoSuchNubeculaFileException("No such directory");
        return responseCreator.createDir(requestedDirectory);
    }


    @Override
    @Transactional
    public List<ResponseFile> loadAll(
            String username, UUID id, String sort, boolean desc
    ) throws NotNubeculaDirectoryException {
        NubeculaFile dir = getOperableDir(username, id);
        if (dir == null || !dir.isDirectory()) throw new NotNubeculaDirectoryException("Not a directory");
        Sort sortQuery = getSort(sort, desc);
        return responseCreator.create(fileRepository.findAllByParentDirectoryId(dir.getId(), sortQuery));
    }



    @Override
    @Transactional
    public List<ResponseFile> loadAllShared(
            String username, UUID id, String sort, boolean desc
    ) throws UsernameNotFoundException {
        NubeculaFile dir = getOperableDir(username, id);
        NubeculaUser user = userService.getByName(username);
        Sort sortQuery = getSort(sort, desc);
        return responseCreator.create(
                fileRepository
                        .findAllByOwnerIdAndParentDirectoryIdAndSharedIsTrue(user.getId(), dir.getId(), sortQuery)
        );
    }


    @Override
    @Transactional
    public List<ResponseFile> loadAllDirectories(String username, UUID id) {
        NubeculaFile dir = getOperableDir(username, id);
        if (dir == null || !dir.isDirectory()) throw new NotNubeculaDirectoryException("Not a directory");
        return responseCreator.createDirs(fileRepository.findAllDirectoriesByParentDirectoryId(dir.getId()));
    }


    @Override
    @Transactional
    public List<ResponseFile> search(String username, String searched, boolean anywhere) {
        NubeculaUser user = userService.getByName(username);
        if (anywhere)
            return responseCreator.create(fileRepository.searchByFilenameAnywhere(searched, user.getId()));
        else return responseCreator.create(fileRepository.searchByFilenameBeginning(searched, user.getId()));
    }


    @Override
    @Transactional
    public List<ResponseFile> searchShared(String username, String searched, boolean anywhere) {
        NubeculaUser user = userService.getByName(username);
        if (anywhere)
            return responseCreator.create(fileRepository.searchAllSharedByFilenameAnywhere(searched, user.getId()));
        else return responseCreator.create(fileRepository.searchAllSharedByFilenameBeginning(searched, user.getId()));
    }


    @Override
    public int getSizeOfDirectory(UUID directoryId) {
        NubeculaFile directory = fileRepository.findById(directoryId).orElse(null);
        if (directory == null || !directory.isDirectory()) return 0;
        return (int) getSizeOfDirectory(directory);
    }


    private long getSizeOfDirectory(NubeculaFile directory) {
        long sum = 0L;
        for (NubeculaFile file : directory.getNubeculaFiles()) {
            if (file.isDirectory()) getSizeOfDirectory(file);
            else sum += file.getSize();
        }
        return sum;
    }


    private Sort getSort(String sort, boolean desc) {
        if (desc) return Sort.by(
                Sort.Order.desc("isDirectory"),
                Sort.Order.desc(sort)
        );
        else return Sort.by(
                Sort.Order.desc("isDirectory"),
                Sort.Order.asc(sort)
        );
    }


    /* <------------------------------------------- UPDATE -------------------------------------------> */

    @Override
    public void rename(UUID id, String newName) {
        NubeculaFile file = load(id);
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
    public void toggleShare(UUID id) {
        NubeculaFile virtualPath = load(id);
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
    public ResponseFile[] moveToTrashBin(String username, List<ResponseFile> replacedFiles) {
        NubeculaFile trashBin = getTrashBin(username);
        return replaceAll(replacedFiles, trashBin);
    }


    @Override
    public ResponseFile[] replace(String username, List<ResponseFile> replacedFiles, UUID targetDirId) {
        NubeculaFile targetDir = getOperableDir(username, targetDirId);
        return replaceAll(replacedFiles, targetDir);
    }


    private ResponseFile[] replaceAll(List<ResponseFile> replacedFiles, NubeculaFile targetDir) {
        int size = replacedFiles.size();
        ResponseFile[] responseFiles = new ResponseFile[size];
        for (int i = 0; i < size; i++) {
            responseFiles[i] = replaceOne(load(replacedFiles.get(i).getId()), targetDir);
        }
        return responseFiles;
    }


    private ResponseFile replaceOne(NubeculaFile replaced, NubeculaFile targetDir) {
        if (replaced == null) throw new NoSuchNubeculaFileException("replaced file ID not found");
        replaced.setParentDirectory(targetDir);
        if (targetDir.isShared()) replaced.setShared(true);
        else if (!targetDir.isShared()) replaced.setShared(false);
        NubeculaFile savedFile = fileRepository.save(replaced);
        if (replaced.isDirectory())
            return responseCreator.createDir(savedFile);
        else return responseCreator.createFile(savedFile);
    }


    /* <------------------------------------------- DELETE -------------------------------------------> */

    @Override
    public void delete(UUID id) {
        NubeculaFile virtualPath = load(id);
        if (virtualPath == null) throw new NoSuchNubeculaFileException("ID not found");
        if (virtualPath.isDirectory()) {
            for (NubeculaFile file : virtualPath.getNubeculaFiles()) {
                if (file.isDirectory()) {
                    delete(file.getId());
                } else {
                    storageService.delete(file.getFileId().toString());
                    userService.deleteFromUserStorageSize(file.getSize());
                    fileRepository.delete(file);
                }
            }
        } else {
            storageService.delete(virtualPath.getFileId().toString());
            userService.deleteFromUserStorageSize(virtualPath.getSize());
        }
        fileRepository.delete(virtualPath);
    }


    @Override
    public void deleteUserData(String username) {
        fileRepository.delete(getRoot(username));
        fileRepository.delete(getRoot(username));
    }


    /* <------------------------------------------- SET FIELD -------------------------------------------> */

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}
