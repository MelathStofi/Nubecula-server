package com.melath.nubecula.storage.service;

import com.melath.nubecula.storage.model.NubeculaFile;
import com.melath.nubecula.storage.model.reponse.ResponseFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
                responseFile = createDir(nubeculaFile);
            } else {
                responseFile = createFile(nubeculaFile);
            }
            return responseFile;
        }).collect(Collectors.toList());
    }

    public ResponseFile createDir(NubeculaFile directory) {
        return ResponseFile.builder()
                .id(directory.getId())
                .filename(directory.getFilename())
                .type(directory.getType())
                .size(directory.getSize())
                .createDate(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(directory.getCreateDate()))
                .modificationDate(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(directory.getModificationDate()))
                .isDirectory(true)
                .shared(directory.isShared())
                .url(baseUrl + "/" + directory.getId())
                .build();
    }

    public ResponseFile createFile(NubeculaFile file) {
        return ResponseFile.builder()
                .id(file.getId())
                .filename(file.getFilename())
                .type(file.getType())
                .extension(file.getExtension())
                .size(file.getSize())
                .createDate(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(file.getCreateDate()))
                .modificationDate(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(file.getModificationDate()))
                .isDirectory(false)
                .shared(file.isShared())
                .url(baseUrl + "/files/" + file.getId())
                .build();
    }

}
