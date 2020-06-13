package com.melath.nubecula.storage.repository;

import com.melath.nubecula.storage.model.NubeculaFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRepository extends JpaRepository<NubeculaFile, UUID> {

    public NubeculaFile findByFileName(String filename);
}
