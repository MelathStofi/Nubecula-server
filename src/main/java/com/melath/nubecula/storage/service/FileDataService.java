package com.melath.nubecula.storage.service;

import com.melath.nubecula.storage.model.NubeculaFile;
import com.melath.nubecula.storage.model.exceptions.NotNubeculaDirectoryException;
import com.melath.nubecula.storage.model.reponse.ResponseFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface FileDataService {
    NubeculaFile store(UUID parentDirId, MultipartFile file, String username);

    List<ResponseFile> loadAll(UUID id, String sort, boolean desc) throws NotNubeculaDirectoryException;

    List<ResponseFile> loadAllShared(String username, String sort, boolean desc) throws UsernameNotFoundException;

    NubeculaFile load(UUID id);

    NubeculaFile load(String username);

    ResponseFile createDirectory(UUID parentDirId, String dirname, String username);

    void createDirectory(String username);

    void rename(UUID id, String newName);

    void delete(UUID id);

    void deleteAll(List<ResponseFile> files);

    void toggleShare(UUID id);

    ResponseFile replace(UUID replaceableId, UUID targetDirId);

    ResponseFile copy(UUID copiedId, UUID targetDirId, String username);

    long getSizeOfDirectory(UUID directoryId);

    List<ResponseFile> search(String searched, String username);

}
