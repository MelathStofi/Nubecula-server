package com.melath.nubecula.storage.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NubeculaFile {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    private String fileName;

    private String type;

    private String extension;

    private int size;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    private boolean isDirectory;

    @ManyToOne
    private NubeculaFile parentDirectory;

    private String owner;

}
