package com.melath.nubecula.storage.model.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ResponseDirectory {

    private UUID id;

    private String name;

    private long size;

    private LocalDateTime createDate;

    private boolean shared;

    private String url;

}
