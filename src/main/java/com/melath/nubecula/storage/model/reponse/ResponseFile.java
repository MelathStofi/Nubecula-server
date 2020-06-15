package com.melath.nubecula.storage.model.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ResponseFile {

    private UUID id;

    private String name;

    private String type;

    private String extension;

    private long size;

    private Date createDate;

    private String url;

}
