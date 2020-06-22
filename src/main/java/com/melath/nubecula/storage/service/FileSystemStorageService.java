package com.melath.nubecula.storage.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.melath.nubecula.storage.config.StorageProperties;
import com.melath.nubecula.storage.model.exceptions.StorageException;
import com.melath.nubecula.storage.model.exceptions.StorageFileNotFoundException;
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
	public void store(MultipartFile file, UUID fileId) {
		String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
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
				Files.copy(inputStream, rootLocation.resolve(fileId.toString()),
					StandardCopyOption.REPLACE_EXISTING);
				log.info("Successfully uploaded " + filename);
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file " + filename, e);
		}
	}

	@Override
	public Set<Path> loadAll(String dir) {
		Path location;
		if (!dir.equals("")) location = Paths.get(this.rootLocation.toString() + "/" + dir); //<-- EZT JAVÍTSD KI!!!
		else location = rootLocation;
		try {
			return Files.walk(location, 1)
				.filter(path -> !path.equals(location))
				.map(location::relativize).collect(Collectors.toSet());
		}
		catch (IOException e) {
			log.error("Failed to read stored files: " + e);
			throw new StorageException("Failed to read stored files", e);
		}

	}

	@Override
	public Path load(String filename) {
		return Paths.get(this.rootLocation.toString() + "/" + filename);
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
				log.error("Could not read file: " + filename);
				throw new StorageFileNotFoundException(
						"Could not read file: " + filename);

			}
		}
		catch (MalformedURLException e) {
			log.error("Could not read file: " + filename + "due to MalformedURLException");
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	@Override
	public boolean delete(String delenda) {
		boolean returnValue = FileSystemUtils.deleteRecursively(Paths.get(rootLocation.toString() + "/" + delenda ).toFile());
		if (returnValue) log.info("Delete: " + delenda);
		return returnValue;
	}

	@Override
	public void deleteAll(String username) {
		FileSystemUtils.deleteRecursively(Paths.get(rootLocation + "/" + username).toFile());
	}

	@Override
	public void rename(String filename, String newName) {
		Path path = Paths.get(rootLocation.toString() + "/" + filename);
		try {
			Files.move(path, path.resolveSibling(newName));
		} catch (IOException e) {
			log.error("Couldn't rename file " + filename + " to: " + newName);
			throw new StorageException("Couldn't rename file ", e);
		}
	}

	@Override
	public void createDirectory(String name) {
		try {
			Files.createDirectories(Paths.get(rootLocation.toString() + "/" + name));
		} catch (IOException e) {
			throw new StorageException("Couldn't create directory " + name + ": Already exists", e);
		}
	}

	@Override
	public void copy(String filename, String newFilename) {
		try {
			Files.copy(
					Paths.get(rootLocation.toString() + "/" + filename),
					Paths.get(rootLocation.toString() + "/" + newFilename)
			);
		} catch (IOException e) {
			throw new StorageException("Couldn't copy file: " + filename);
		}
	}

	@Override
	public void init() {
		try {
			Files.createDirectories(rootLocation);
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage ", e);
		}
	}
}
