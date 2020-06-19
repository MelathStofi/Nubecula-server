package com.melath.nubecula.storage.repository;

import com.melath.nubecula.storage.model.NubeculaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.stream.Stream;

public interface FileRepository extends JpaRepository<NubeculaFile, UUID> {


    NubeculaFile findByFilename(String filename);


    @Query(
            value="SELECT * FROM nubecula_file WHERE parent_directory_id = :parentDirectoryId ORDER BY is_directory DESC, :sort DESC",
            nativeQuery=true
    )
    Stream<NubeculaFile> findAllIsDirDescSortDesc(
            @Param("parentDirectoryId") UUID parentDirectoryId,
            @Param("sort") String sort
    );

    @Query(
            value="SELECT * FROM nubecula_file WHERE parent_directory_id = :parentDirectoryId ORDER BY is_directory DESC, :sort",
            nativeQuery=true
    )
    Stream<NubeculaFile> findAllIsDirDescSortAsc(
            @Param("parentDirectoryId") UUID parentDirectoryId,
            @Param("sort") String sort
    );


    @Query(
            value="SELECT * FROM nubecula_file WHERE parent_directory_id = :parentDirectoryId AND shared = TRUE ORDER BY is_directory DESC, :sort DESC",
            nativeQuery=true
    )
    Stream<NubeculaFile> findAllSharedDesc(
            @Param("parentDirectoryId") UUID parentDirectoryId,
            @Param("sort") String sort
    );


    @Query(
            value="SELECT * FROM nubecula_file WHERE parent_directory_id = :parentDirectoryId AND shared = TRUE ORDER BY is_directory DESC, :sort",
            nativeQuery=true
    )
    Stream<NubeculaFile> findAllSharedAsc(
            @Param("parentDirectoryId") UUID parentDirectoryId,
            @Param("sort") String sort
    );


    @Query(
            value="SELECT IF((nf.filename = :filename), TRUE, FALSE) FROM nubecula_file nf WHERE nf.parent_directory_id = :parentDirectoryId",
            nativeQuery=true
    )
    boolean doesFileAlreadyExist(@Param("filename") String filename, @Param("parentDirectoryId") UUID parentDirectoryId);


    @Query(
            value="DELETE FROM nubecula_file WHERE parent_directory_id = :parentDirectoryId",
            nativeQuery=true
    )
    void deleteAllByParentDirectoryId(@Param("parentDirectoryId") UUID parentDirectoryId);


}
