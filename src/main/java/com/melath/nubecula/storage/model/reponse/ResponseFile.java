package com.melath.nubecula.storage.model.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseFile {

    private UUID id;

    private String filename;

    private String extension;

    private String type;

    private long size;

    private String createDate;

    private String modificationDate;

    private boolean isDirectory;

    private boolean shared;

    private String url;

}
