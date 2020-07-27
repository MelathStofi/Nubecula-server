package com.melath.nubecula.storage.service;

import com.melath.nubecula.storage.model.exception.NoSuchNubeculaFileException;
import com.melath.nubecula.user.model.entity.NubeculaUser;
import com.melath.nubecula.storage.model.entity.NubeculaFile;
import com.melath.nubecula.storage.model.exception.NotNubeculaDirectoryException;
import com.melath.nubecula.storage.model.reponse.ResponseFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface FileDataService {

    NubeculaFile store(String username, UUID parentDirId, MultipartFile file);

    List<ResponseFile> loadAll(String username, UUID id, String sort, boolean desc) throws NotNubeculaDirectoryException;

    List<ResponseFile> loadAllShared(String username, UUID id, String sort, boolean desc) throws UsernameNotFoundException;

    List<ResponseFile> loadAllDirectories(String username, UUID id);

    List<ResponseFile> loadTrashBin(String username, String sort, boolean desc);

    ResponseFile loadDirectory(String username, UUID id) throws NoSuchNubeculaFileException;

    ResponseFile loadSharedDirectory(String username, UUID id) throws NoSuchNubeculaFileException;

    NubeculaFile load(UUID id);

    ResponseFile createDirectory(String username, UUID parentDirId, String dirname);

    void createDirectory(String dirname, NubeculaUser user);

    void rename(UUID id, String newName);

    void toggleShare(UUID id);

    void delete(UUID id);

    ResponseFile[] moveToTrashBin(String username, List<ResponseFile> replacedFiles);

    ResponseFile[] replace(String username, List<ResponseFile> replacedFiles, UUID targetDirId);

    ResponseFile[] copy(String username, List<ResponseFile> copiedFiles, UUID targetDirId);

    int getSizeOfDirectory(UUID directoryId);

    List<ResponseFile> search(String username, String searched, boolean anywhere);

    List<ResponseFile> searchShared(String username, String searched, boolean anywhere);

    void deleteUserData(String username);
}
