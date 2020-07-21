package com.melath.nubecula.storage.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NubeculaFile {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(columnDefinition = "BINARY(16)")
    private UUID fileId;

    private String filename;

    private String type;

    private String extension;

    private long size;

    @Basic
    private LocalDateTime createDate;

    private LocalDateTime modificationDate;

    private boolean isDirectory;

    @ManyToOne(fetch = FetchType.LAZY)
    private NubeculaFile parentDirectory;

    @OneToMany(mappedBy = "parentDirectory")
    private Set<NubeculaFile> nubeculaFiles;

    private String owner;

    private boolean shared;



    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(LocalDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public NubeculaFile getParentDirectory() {
        return parentDirectory;
    }

    public void setParentDirectory(NubeculaFile parentDirectory) {
        this.parentDirectory = parentDirectory;
    }

    public Set<NubeculaFile> getNubeculaFiles() {
        return nubeculaFiles;
    }

    public void setNubeculaFiles(Set<NubeculaFile> nubeculaFiles) {
        this.nubeculaFiles = nubeculaFiles;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    @Override
    public String toString() {
        return "NubeculaFile{" +
                "id=" + id +
                ", fileId=" + fileId +
                ", filename='" + filename + '\'' +
                ", type='" + type + '\'' +
                ", extension='" + extension + '\'' +
                ", size=" + size +
                ", createDate=" + createDate +
                ", isDirectory=" + isDirectory +
                ", parentDirectory=" + parentDirectory +
                ", nubeculaFiles=" + nubeculaFiles +
                ", owner='" + owner + '\'' +
                ", shared=" + shared +
                '}';
    }
}
