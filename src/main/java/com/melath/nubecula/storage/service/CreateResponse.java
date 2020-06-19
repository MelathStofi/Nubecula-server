package com.melath.nubecula.storage.service;

import com.melath.nubecula.storage.model.NubeculaFile;
import com.melath.nubecula.storage.model.reponse.ResponseFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CreateResponse {

    @Value("${base.url}")
    private String baseUrl;

    @Transactional
    public List<ResponseFile> create(Stream<NubeculaFile> filesInDirectory) {
        return filesInDirectory.map(nubeculaFile -> {
            ResponseFile responseFile;
            if (nubeculaFile.isDirectory()) {
                responseFile = ResponseFile.builder()
                        .id(nubeculaFile.getId())
                        .filename(nubeculaFile.getFilename())
                        .size(nubeculaFile.getSize())
                        .createDate(nubeculaFile.getCreateDate())
                        .isDirectory(true)
                        .shared(nubeculaFile.isShared())
                        .url(baseUrl + "/" + nubeculaFile.getId())
                        .build();
            } else {
                responseFile = ResponseFile.builder()
                        .id(nubeculaFile.getId())
                        .filename(nubeculaFile.getFilename())
                        .type(nubeculaFile.getType())
                        .extension(nubeculaFile.getExtension())
                        .size(nubeculaFile.getSize())
                        .createDate(nubeculaFile.getCreateDate())
                        .isDirectory(false)
                        .shared(nubeculaFile.isShared())
                        .url(baseUrl + "/files/" + nubeculaFile.getId())
                        .build();
            }
            return responseFile;
        }).collect(Collectors.toList());
    }

}
