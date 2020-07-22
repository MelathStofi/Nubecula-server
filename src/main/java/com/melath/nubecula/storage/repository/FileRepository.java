package com.melath.nubecula.storage.repository;

import com.melath.nubecula.storage.model.entity.NubeculaFile;
import com.melath.nubecula.storage.repository.custom.FileRepositoryCustom;
import com.melath.nubecula.user.model.entity.NubeculaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.stream.Stream;

public interface FileRepository extends JpaRepository<NubeculaFile, UUID>, FileRepositoryCustom {


    NubeculaFile findByFilename(String filename);

    long countByFilenameAndParentDirectoryId(String filename, UUID parentDirectoryId);

    @Query(value="SELECT nf FROM NubeculaFile nf WHERE nf.parentDirectory.id IS NOT NULL AND nf.owner = :owner AND nf.filename LIKE %:searched% ORDER BY nf.isDirectory DESC")
    Stream<NubeculaFile> searchByFilenameAnywhere(@Param("searched") String searched, @Param("owner") NubeculaUser owner);

    @Query(value="SELECT nf FROM NubeculaFile nf WHERE nf.parentDirectory.id IS NOT NULL AND nf.owner = :owner AND nf.filename LIKE :searched% ORDER BY nf.isDirectory DESC")
    Stream<NubeculaFile> searchByFilenameBeginning(@Param("searched") String searched, @Param("owner") NubeculaUser owner);

    @Query(
            value="SELECT * FROM file WHERE parent_directory_id = :parentDirectoryId ORDER BY is_directory DESC, :sort DESC",
            nativeQuery=true
    )
    Stream<NubeculaFile> findAllIsDirDescSortDesc(
            @Param("parentDirectoryId") UUID parentDirectoryId,
            @Param("sort") String sort
    );

    @Query(
            value="SELECT * FROM file WHERE parent_directory_id = :parentDirectoryId ORDER BY is_directory DESC, :sort",
            nativeQuery=true
    )
    Stream<NubeculaFile> findAllIsDirDescSortAsc(
            @Param("parentDirectoryId") UUID parentDirectoryId,
            @Param("sort") String sort
    );


    @Query(
            value="SELECT * FROM file WHERE parent_directory_id = :parentDirectoryId AND shared = TRUE ORDER BY is_directory DESC, :sort DESC",
            nativeQuery=true
    )
    Stream<NubeculaFile> findAllSharedDesc(
            @Param("parentDirectoryId") UUID parentDirectoryId,
            @Param("sort") String sort
    );


    @Query(
            value="SELECT * FROM file WHERE parent_directory_id = :parentDirectoryId AND shared = TRUE ORDER BY is_directory DESC, :sort",
            nativeQuery=true
    )
    Stream<NubeculaFile> findAllSharedAsc(
            @Param("parentDirectoryId") UUID parentDirectoryId,
            @Param("sort") String sort
    );


    @Query(
            value="SELECT CASE WHEN nf.filename = :filename AND nf.extension = :extension OR nf.filename IS NOT NULL THEN TRUE ELSE FALSE END FROM file nf WHERE nf.parent_directory_id = :parentDirectoryId",
            nativeQuery=true
    )
    boolean existsInDirectory(@Param("filename") String filename, @Param("extension") String extension, @Param("parentDirectoryId") UUID parentDirectoryId);



}
