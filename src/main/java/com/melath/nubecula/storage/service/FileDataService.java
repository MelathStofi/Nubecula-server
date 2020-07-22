package com.melath.nubecula.storage.service;

import com.melath.nubecula.user.model.entity.NubeculaUser;
import com.melath.nubecula.storage.model.entity.NubeculaFile;
import com.melath.nubecula.storage.model.exception.NotNubeculaDirectoryException;
import com.melath.nubecula.storage.model.reponse.ResponseFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FileDataService {
    NubeculaFile store(UUID parentDirId, MultipartFile file, NubeculaUser user);

    List<ResponseFile> loadAll(UUID id, String sort, boolean desc) throws NotNubeculaDirectoryException;

    List<ResponseFile> loadAllShared(NubeculaUser user, String sort, boolean desc) throws UsernameNotFoundException;

    NubeculaFile load(UUID id);

    ResponseFile createDirectory(UUID parentDirId, String dirname, NubeculaUser user);

    Map<String, UUID> createRootDirectory(NubeculaUser user);

    void rename(UUID id, String newName);

    void delete(UUID id);

    void toggleShare(UUID id);

    ResponseFile replace(UUID replaceableId, UUID targetDirId);

    ResponseFile copy(UUID copiedId, UUID targetDirId, NubeculaUser user);

    int getSizeOfDirectory(UUID directoryId);

    List<ResponseFile> search(String searched, boolean anywhere, NubeculaUser user);

}
