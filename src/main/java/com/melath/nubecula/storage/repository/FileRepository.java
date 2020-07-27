package com.melath.nubecula.storage.repository;

import com.melath.nubecula.storage.model.entity.NubeculaFile;
import com.melath.nubecula.storage.repository.custom.FileRepositoryCustom;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.stream.Stream;

public interface FileRepository extends JpaRepository<NubeculaFile, UUID>, FileRepositoryCustom {


    NubeculaFile findByFilename(String filename);


    long countByFilenameAndParentDirectoryId(String filename, UUID parentDirectoryId);


    NubeculaFile findFirstByOwnerUsernameAndType(String username, String type);


    Stream<NubeculaFile> findAllByParentDirectoryId(
            UUID parentDirectoryId,
            Sort sort
    );

    NubeculaFile findByIdAndOwnerUsernameAndSharedIsTrue(UUID id, String username);


    @Query(value="SELECT f FROM file f WHERE f.parentDirectory.id IS NOT NULL AND f.owner.id = :ownerId AND f.filename LIKE %:searched% ORDER BY f.isDirectory DESC, f.filename")
    Stream<NubeculaFile> searchByFilenameAnywhere(@Param("searched") String searched, @Param("ownerId") int ownerId);


    @Query(value="SELECT f FROM file f WHERE f.parentDirectory.id IS NOT NULL AND f.owner.id = :ownerId AND f.filename LIKE :searched% ORDER BY f.isDirectory DESC, f.filename")
    Stream<NubeculaFile> searchByFilenameBeginning(@Param("searched") String searched, @Param("ownerId") int ownerId);


    @Query(value="SELECT f FROM file f WHERE f.parentDirectory.id IS NOT NULL AND f.owner.id = :ownerId AND f.shared = TRUE AND f.filename LIKE %:searched% ORDER BY f.isDirectory DESC, f.filename")
    Stream<NubeculaFile> searchAllSharedByFilenameAnywhere(@Param("searched") String searched, @Param("ownerId") int ownerId);


    @Query(value="SELECT f FROM file f WHERE f.parentDirectory.id IS NOT NULL AND f.owner.id = :ownerId AND f.shared = TRUE AND f.filename LIKE :searched% ORDER BY f.isDirectory DESC, f.filename")
    Stream<NubeculaFile> searchAllSharedByFilenameBeginning(@Param("searched") String searched, @Param("ownerId") int ownerId);


    Stream<NubeculaFile> findAllByOwnerIdAndParentDirectoryIdAndSharedIsTrue(int ownerId, UUID parentDirectoryId, Sort sort);

    @Query(value="SELECT f FROM file f WHERE f.parentDirectory.id = ?1 AND f.isDirectory = TRUE ORDER BY f.filename")
    Stream<NubeculaFile> findAllDirectoriesByParentDirectoryId(UUID parentDirectoryId);

    @Query(
            value="SELECT CASE WHEN nf.filename = :filename AND nf.extension = :extension OR nf.filename IS NOT NULL THEN TRUE ELSE FALSE END FROM file nf WHERE nf.parent_directory_id = :parentDirectoryId",
            nativeQuery=true
    )
    boolean existsInDirectory(@Param("filename") String filename, @Param("extension") String extension, @Param("parentDirectoryId") UUID parentDirectoryId);



}
