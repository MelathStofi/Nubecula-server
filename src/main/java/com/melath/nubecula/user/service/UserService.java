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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    private final EmailSenderService emailSenderService;

    private final FileDataService fileDataService;

    private final long storageSize = 3000000000000L;

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


    public void add(NubeculaUser user) {
        userRepository.save(user);
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


    public ResponseUser getPublicUser(String username) {
        NubeculaUser user = getByName(username);
        return createResponseUser(user);
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
        NubeculaUser newUser = NubeculaUser
                .builder()
                .username(username)
                .password(encoder.encode(userCredentials.getPassword()))
                .role(Role.USER)
                .email(email)
                .registrationDate(LocalDateTime.now())
                .storage(storageSize)
                .inStorage(0L)
                .build();
        userRepository.save(newUser);
        Map<String, UUID> ids = fileDataService.createRootDirectory(newUser);
        newUser.setRootDirectoryId(ids.get("root"));
        newUser.setTrashBinId(ids.get("trashBin"));
        userRepository.save(newUser);
        emailSenderService.sendEmail(email, username);
    }


    public void renameUser(String username, String newName) {
        NubeculaUser user = getByName(username);
        user.setUsername(newName);
        userRepository.save(user);
        fileDataService.rename(user.getRootDirectoryId(), newName);
    }


    public void setUserDescription(String username, String description) {
        NubeculaUser user = getByName(username);
        user.setDescription(description);
        userRepository.save(user);
    }


    public void deleteUser(String username) {
        NubeculaUser user = getByName(username);
        fileDataService.delete(user.getRootDirectoryId());
        userRepository.delete(user);
    }


    public UserRepository getUserRepository() {
        return this.userRepository;
    }


    public boolean addToUserStorageSize(NubeculaUser user, List<MultipartFile> files) throws UsernameNotFoundException {
        long sumSize = files.stream().mapToLong(MultipartFile::getSize).sum() + user.getInStorage();
        if (user.getStorage() >= sumSize) {
            user.setInStorage(sumSize);
            userRepository.save(user);
            return true;
        }
        return false;
    }


    public boolean addToUserStorageSize(NubeculaUser user, long size) throws UsernameNotFoundException {
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
                .storage(user.getStorage())
                .inStorage(user.getInStorage())
                .description(user.getDescription())
                .build();
    }
}

