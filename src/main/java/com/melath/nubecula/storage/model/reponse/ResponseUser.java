package com.melath.nubecula.storage.model.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ResponseUser {

    private String username;

    private LocalDateTime registrationDate;

    private long storage;

    private long inStorage;
}
