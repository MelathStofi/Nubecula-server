package com.melath.nubecula.storage;

import com.melath.nubecula.storage.config.StorageProperties;
import com.melath.nubecula.storage.model.exceptions.StorageException;
import com.melath.nubecula.storage.service.FileSystemStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class FileSystemStorageServiceTest {

    private String rootLocation;

    private FileSystemStorageService storage;

    @BeforeEach
    public void setUp() {
        StorageProperties props = new StorageProperties();
        props.setLocation("test-dir");
        storage = new FileSystemStorageService(props);
        storage.init();
    }

    @AfterEach
    public void terminate() {
        storage.deleteAll();
    }

    @Test
    public void saveFile() {
        MultipartFile file = new MockMultipartFile(
                "data",
                "filename.txt",
                "text/plain",
                "something".getBytes()
        );
        storage.store(file, "");
        Set<Path> files = storage.loadAll("");
        assertThat(files)
                .hasSize(1)
                .containsOnly(Paths.get(file.getOriginalFilename()));

    }

    @Test
    public void saveEmptyFile() {
        MultipartFile file = new MockMultipartFile(
                "data",
                "filename.txt",
                "text/plain",
                "".getBytes()
        );
        assertThrows(StorageException.class, () -> storage.store(file, ""));
    }

    @Test
    public void saveFileWithTwoDotsInFilename() {
        MultipartFile file = new MockMultipartFile(
                "data",
                "../filename.txt",
                "text/plain",
                "something".getBytes()
        );
        assertThrows(StorageException.class, () -> storage.store(file, ""));
    }

    @Test
    public void deleteFile() {
        MultipartFile file = new MockMultipartFile(
                "data",
                "filename.txt",
                "text/plain",
                "something".getBytes()
        );
        storage.store(file, "");
        boolean hasBeenDeleted = storage.delete("/" + file.getOriginalFilename());
        Set<Path> files = storage.loadAll("");
        assertThat(files).hasSize(0);
    }

    @Test
    public void deleteDirectoryWithFileAndDirectory() throws FileAlreadyExistsException {
        MultipartFile file = new MockMultipartFile(
                "data",
                "filename.txt",
                "text/plain",
                "something".getBytes()
        );
        storage.createDirectory("newFolder");
        storage.store(file, "/newFolder");
        storage.createDirectory("yetAnotherFolder", "/newFolder");
        assertTrue(storage.delete("/newFolder"));
    }

    @Test
    public void renameFile() {
        /*MultipartFile file = new MockMultipartFile(
                "data",
                "filename.txt",
                "text/plain",
                "something".getBytes()
        );
        storage.store(file, "");
        storage.rename("newname.pdf", "");
        Set<Path> files = storage.loadAll("");
        Path path = files.stream().findFirst().get();
        assertThat(path).endsWith(Paths.get(rootLocation + "/newname"));*/
    }
}