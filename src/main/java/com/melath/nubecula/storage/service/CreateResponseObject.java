package com.melath.nubecula.storage.service;

import com.melath.nubecula.storage.model.NubeculaFile;
import com.melath.nubecula.storage.model.reponse.ResponseDirectory;
import com.melath.nubecula.storage.model.reponse.ResponseFile;
import com.melath.nubecula.storage.model.reponse.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class CreateResponseObject {

    @Value("${base.url}")
    private String baseUrl;

    private final FileDataService fileDataService;

    @Autowired
    public CreateResponseObject(
            FileDataService fileDataService
    ) {
        this.fileDataService = fileDataService;
    }

    public ResponseObject create(UUID id) {
        Set<ResponseDirectory> directories = new HashSet<>();
        Set<ResponseFile> files = new HashSet<>();

        Set<NubeculaFile> filesInDirectory = fileDataService.loadAll(id);
        filesInDirectory.forEach(nubeculaFile -> {
            if (nubeculaFile.isDirectory()) {
                ResponseDirectory responseDirectory = new ResponseDirectory(
                        nubeculaFile.getId(),
                        nubeculaFile.getFileName(),
                        nubeculaFile.getSize(),
                        nubeculaFile.getCreateDate(),
                        baseUrl + "/" + id
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
                        baseUrl + "/files/" + id
                );
                files.add(responseFile);
            }
        });
        return ResponseObject.builder().directories(directories).files(files).build();

    }

}
