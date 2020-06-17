package com.melath.nubecula.storage.service;

import com.melath.nubecula.storage.model.NubeculaFile;
import com.melath.nubecula.storage.model.reponse.ResponseDirectory;
import com.melath.nubecula.storage.model.reponse.ResponseFile;
import com.melath.nubecula.storage.model.reponse.ResponseObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CreateResponseObject {

    @Value("${base.url}")
    private String baseUrl;


    public ResponseObject create(Set<NubeculaFile> filesInDirectory) {
        Set<ResponseDirectory> directories = new HashSet<>();
        Set<ResponseFile> files = new HashSet<>();

        filesInDirectory.forEach(nubeculaFile -> {
            if (nubeculaFile.isDirectory()) {
                ResponseDirectory responseDirectory = new ResponseDirectory(
                        nubeculaFile.getId(),
                        nubeculaFile.getFileName(),
                        nubeculaFile.getSize(),
                        nubeculaFile.getCreateDate(),
                        nubeculaFile.isShared(),
                        baseUrl + "/" + nubeculaFile.getId()
                );
                directories.add(responseDirectory);
            } else {
                ResponseFile responseFile = new ResponseFile(
                        nubeculaFile.getId(),
                        nubeculaFile.getFileName(),
                        nubeculaFile.getExtension(),
                        nubeculaFile.getType(),
                        nubeculaFile.getSize(),
                        nubeculaFile.getCreateDate(),
                        nubeculaFile.isShared(),
                        baseUrl + "/files/" + nubeculaFile.getId()
                );
                files.add(responseFile);
            }
        });
        return ResponseObject.builder().directories(directories).files(files).build();

    }

}
