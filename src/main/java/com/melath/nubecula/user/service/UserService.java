package com.melath.nubecula.user.service;

import com.melath.nubecula.user.model.entity.NubeculaUser;
import com.melath.nubecula.user.model.entity.Role;
import com.melath.nubecula.user.model.exception.NoSuchUserException;
import com.melath.nubecula.user.model.request.UserCredentials;
import com.melath.nubecula.user.model.exception.EmailAlreadyExistsException;
import com.melath.nubecula.user.model.exception.UsernameAlreadyExistsException;
import com.melath.nubecula.user.repository.UserRepository;
import com.melath.nubecula.user.model.response.ResponseUser;
import com.melath.nubecula.storage.service.FileDataService;
import com.melath.nubecula.util.NubeculaUtils;
import lombok.extern.slf4j.Slf4j;
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
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    private final EmailSenderService emailSenderService;

    private final FileDataService fileDataService;

    private final long storageSize = 3000000000L;

    @Autowired
    public UserService(
            UserRepository userRepository,
            EmailSenderService emailSenderService,
            PasswordEncoder encoder,
            FileDataService fileDataService
    ) {
        this.userRepository = userRepository;
        this.emailSenderService = emailSenderService;
        this.encoder = encoder;
        this.fileDataService = fileDataService;
    }


    public NubeculaUser getByName(String name) throws UsernameNotFoundException {
        return userRepository.findByUsername(name)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }


    public ResponseUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            throw new NoSuchUserException("No such user");
        }
        String currentUserName = authentication.getName();
        NubeculaUser user = getByName(currentUserName);
        return createResponseUser(user);

    }


    @Transactional
    public List<ResponseUser> getAllUsers() {
        return userRepository.findAllUsers().map(this::createResponseUser
        ).collect(Collectors.toList());
    }


    public ResponseUser getPublicUser(String username) throws NoSuchUserException {
        NubeculaUser user = userRepository.findFirstByUsername(username);
        if (user == null) throw new NoSuchUserException("Username not found");
        else return createResponseUser(user);
    }


    public void signUp(UserCredentials userCredentials) throws AuthenticationException {

        String username = userCredentials.getUsername();
        String email = userCredentials.getEmail();
        String password = userCredentials.getPassword();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException();
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        createUser(username, password, email);
        emailSenderService.sendEmail(email, username);
    }


    public void createUser(String username, String password, String email) {
        NubeculaUser newUser = NubeculaUser.builder()
                .username(username)
                .password(encoder.encode(password))
                .role(Role.USER)
                .email(email)
                .registrationDate(LocalDateTime.now())
                .storage(storageSize)
                .inStorage(0L)
                .build();
        NubeculaUser useEntity = userRepository.saveAndFlush(newUser);
        fileDataService.createDirectory("root directory", useEntity);
        fileDataService.createDirectory("trash bin", useEntity);
    }


    @Transactional
    public List<ResponseUser> searchUser(String searched, boolean anywhere) {
        if (anywhere) return userRepository.searchUsersAnywhere(searched).map(this::createResponseUser
        ).collect(Collectors.toList());
        return userRepository.searchUsersBeginning(searched).map(this::createResponseUser
        ).collect(Collectors.toList());
    }


    public Byte[] storeProfilePicture(String username, MultipartFile file) throws IOException {
        NubeculaUser user = getByName(username);
        Byte[] byteObjects = new Byte[file.getBytes().length];
        int i = 0;
        for (byte b : file.getBytes()){
            byteObjects[i++] = b;
        }
        user.setProfilePicture(byteObjects);
        return userRepository.save(user).getProfilePicture();

    }


    public void renameUser(String username, String newName) {
        NubeculaUser user = getByName(username);
        user.setUsername(newName);
        userRepository.save(user);
    }


    public void setUserDescription(String username, String description) {
        NubeculaUser user = getByName(username);
        user.setDescription(description);
        userRepository.save(user);
    }


    public void deleteUser(String username) {
        NubeculaUser user = getByName(username);
        fileDataService.deleteUserData(username);
        userRepository.delete(user);
    }


    public UserRepository getUserRepository() {
        return this.userRepository;
    }


    public boolean addToUserStorageSize(String username, MultipartFile file) throws UsernameNotFoundException {
        NubeculaUser user = getByName(username);
        long sumSize = file.getSize() + user.getInStorage();
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

    private ResponseUser createResponseUser(NubeculaUser user) {
        return ResponseUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .registrationDate(user.getRegistrationDate())
                .storage(NubeculaUtils.getSizeString(user.getStorage()))
                .inStorage(NubeculaUtils.getSizeString(user.getInStorage()))
                .description(user.getDescription())
                .build();
    }
}

