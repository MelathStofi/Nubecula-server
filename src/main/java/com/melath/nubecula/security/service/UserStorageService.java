package com.melath.nubecula.security.service;

import com.melath.nubecula.security.model.NubeculaUser;
import com.melath.nubecula.security.model.Role;
import com.melath.nubecula.security.model.request.UserCredentials;
import com.melath.nubecula.security.model.exception.EmailAlreadyExistsException;
import com.melath.nubecula.security.model.exception.UsernameAlreadyExistsException;
import com.melath.nubecula.security.repository.UserRepository;
import com.melath.nubecula.storage.model.reponse.ResponseUser;
import com.melath.nubecula.storage.service.FileDataService;
import com.melath.nubecula.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserStorageService {

    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    private final EmailSenderService emailSenderService;

    private final StorageService storageService;

    private final FileDataService fileDataService;

    private final long storageSize = 3000000000L;

    @Autowired
    public UserStorageService(
            UserRepository userRepository,
            EmailSenderService emailSenderService,
            PasswordEncoder encoder,
            StorageService storageService,
            FileDataService fileDataService
    ) {
        this.userRepository = userRepository;
        this.emailSenderService = emailSenderService;
        this.encoder = encoder;
        this.storageService = storageService;
        this.fileDataService = fileDataService;
    }


    public void add(NubeculaUser user) {
        userRepository.save(user);
    }


    public NubeculaUser getByName(String name) throws UsernameNotFoundException {
        return userRepository.findByUsername(name)
                .orElseThrow(() -> new UsernameNotFoundException("Username is not found"));
    }


    @Transactional
    public List<ResponseUser> getAllUsers() {
        return userRepository.findAllUsers().map(user ->
            new ResponseUser(user.getUsername(), user.getRegistrationDate(), user.getStorage(), user.getInStorage())
        ).collect(Collectors.toList());
    }


    public void signUp(UserCredentials userCredentials) throws AuthenticationException {

        String username = userCredentials.getUsername();
        String email = userCredentials.getEmail();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException();
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException();
        }
        fileDataService.createDirectory(username);
        userRepository.save(NubeculaUser
                .builder()
                .username(username)
                .password(encoder.encode(userCredentials.getPassword()))
                .role(Role.USER)
                .email(email)
                .registrationDate(LocalDateTime.now())
                .storage(storageSize)
                .inStorage(0L)
                .build()
        );
        emailSenderService.sendEmail(email, username);
    }


    public void renameUserData(String username, String newName) {
        UUID userId = fileDataService.load(username).getId();
        fileDataService.rename(userId, newName);
    }


    public void deleteUserData(String username) {
        UUID userId = fileDataService.load(username).getId();
        fileDataService.delete(userId);
        storageService.deleteAll(username);
    }


    public UserRepository getUserRepository() {
        return this.userRepository;
    }


    public boolean addToUserStorageSize(String username, Set<MultipartFile> files) throws UsernameNotFoundException {
        NubeculaUser user = getByName(username);
        long sumSize = files.stream().mapToLong(MultipartFile::getSize).sum() + user.getInStorage();
        if (user.getStorage() >= sumSize) {
            user.setInStorage(sumSize);
            userRepository.save(user);
            return true;
        }
        return false;
    }


    public boolean addToUserStorageSize(String username, long size) throws UsernameNotFoundException {
        NubeculaUser user = getByName(username);
        long sumSize = user.getInStorage() + size;
        if (user.getStorage() >= sumSize) {
            user.setInStorage(sumSize);
            userRepository.save(user);
            return true;
        }
        return false;
    }


    public void deleteFromUserStorageSize(long size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            NubeculaUser user = getByName(currentUserName);
            user.setInStorage(user.getInStorage() - size);
            userRepository.save(user);
        }
    }

}

