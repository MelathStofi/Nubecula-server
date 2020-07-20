package com.melath.nubecula.storage.model.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseUser {

    private String username;

    private LocalDateTime registrationDate;

    private long storage;

    private long inStorage;
}
