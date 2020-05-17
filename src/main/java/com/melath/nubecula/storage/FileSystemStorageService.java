package com.melath.nubecula.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class FileSystemStorageService implements StorageService {

	private final Path rootLocation;

	@Autowired
	public FileSystemStorageService(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
	}

	@Override
	public void store(MultipartFile file, String dir) {
		String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
		Path currentLocation = Paths.get(this.rootLocation.toString() + dir);
		try {
			if (file.isEmpty()) {
				log.error("Failed to store empty file " + filename);
				throw new StorageException("Failed to store empty file " + filename);
			}
			if (filename.contains("..")) {
				// This is a security check
				log.error("Cannot store file with relative path outside current directory "
						+ filename);
				throw new StorageException(
						"Cannot store file with relative path outside current directory "
								+ filename);
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, currentLocation.resolve(filename),
					StandardCopyOption.REPLACE_EXISTING);
				log.info("Successfully uploaded " + filename + " to: " + currentLocation);
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file " + filename, e);
		}
	}

	@Override
	public Set<Path> loadAll(String dir) {
		Path location = Paths.get(this.rootLocation.toString() + "/" + dir);
		try {
			return Files.walk(location, 1)
				.filter(path -> !path.equals(location))
				.map(location::relativize).collect(Collectors.toSet());
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}

	}

	@Override
	public Path load(String filename) {
		return rootLocation.resolve(filename);
	}

	@Override
	public Resource loadAsResource(String filename) {
		try {
			Path file = load(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			}
			else {
				throw new StorageFileNotFoundException(
						"Could not read file: " + filename);

			}
		}
		catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	@Override
	public void createDirectory(String dirName, String dir) {
		String fullPath = rootLocation.toString() + "/" + dir + "/" + dirName;
		try {
			Files.createDirectory(Paths.get(fullPath));
			log.info("Create directory: " + dirName + " in: " + fullPath);
		} catch (IOException e) {
			log.error("Could not create directory in: " + fullPath);
			throw new StorageException("Could not create directory", e);
		}
	}

	@Override
	public boolean delete(String delenda) {
		boolean returnValue = FileSystemUtils.deleteRecursively(Paths.get(rootLocation.toString() + delenda ).toFile());
		if (returnValue) log.info("Delete: " + delenda);
		return returnValue;
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}

	@Override
	public void rename(String newName, String location) {
		Path path = Paths.get(rootLocation.toString() + location);
		try {
			System.out.println(path);
			Files.move(path, path.resolveSibling(newName));
		} catch (IOException e) {
			log.error("Couldn't rename file at " + location + " to: " + newName);
			throw new StorageException("Couldn't rename file", e);
		}
	}

	@Override
	public void init() {
		try {
			Files.createDirectories(rootLocation);
			Files.createDirectory(Paths.get(rootLocation.toString() + "/ize"));
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}
}
