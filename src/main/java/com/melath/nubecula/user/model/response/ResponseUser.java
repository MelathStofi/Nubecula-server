package com.melath.nubecula.user.model.response;

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

    private int id;

    private String username;

    private LocalDateTime registrationDate;

    private String storage;

    private String inStorage;

    private String description;
}
