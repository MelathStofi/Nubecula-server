package com.melath.nubecula.storage.repository;

import com.melath.nubecula.storage.model.NubeculaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface FileRepository extends JpaRepository<NubeculaFile, UUID> {

    NubeculaFile findByFileName(String filename);

    @Query(
            value="SELECT * FROM nubecula_file WHERE parent_directory_id = :parentDirectoryId",
            nativeQuery=true
    )
    Set<NubeculaFile> findAllByParentDirectoryId(@Param("parentDirectoryId") UUID parentDirectoryId);

    @Query(
            value="SELECT CASE WHEN (nf.file_name = :filename) THEN TRUE ELSE FALSE END" +
                    "FROM nubecula_file nf WHERE nf.parent_directory_id = hex(:parentDirectoryId)",
            nativeQuery=true
    )
    boolean doesFileAlreadyExist(@Param("filename") String filename, @Param("parentDirectoryId") UUID parentDirectoryId);
}
