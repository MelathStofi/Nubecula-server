package com.melath.nubecula.security.model;

import lombok.*;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NubeculaUser {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    private String email;

    private LocalDateTime registrationDate;

    @ElementCollection
    @Singular
    @Cascade(value = {CascadeType.DELETE})
    private List<Role> roles;

}
