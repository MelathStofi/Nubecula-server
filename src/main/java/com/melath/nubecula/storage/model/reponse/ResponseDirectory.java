package com.melath.nubecula.storage.model.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ResponseDirectory {

    private UUID id;

    private String name;

    private long size;

    private Date createDate;

    private String url;

}
