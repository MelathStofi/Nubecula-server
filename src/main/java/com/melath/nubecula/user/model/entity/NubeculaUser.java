package com.melath.nubecula.user.model.entity;

import com.melath.nubecula.storage.model.entity.NubeculaFile;
import lombok.*;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Entity(name = "user")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NubeculaUser {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    private String email;

    private LocalDateTime registrationDate;

    private long storage;

    private long inStorage;

    private String description;

    @Lob
    private Byte[] profilePicture;

    @ManyToMany
    @JoinTable(name = "friends", joinColumns =@JoinColumn(name = "user_id"), inverseJoinColumns=@JoinColumn(name="friend_id"))
    @Singular
    private Set<NubeculaUser> friends;

    @OneToMany( mappedBy = "owner" )
    private Set<NubeculaFile> files;

    @ManyToMany
    @JoinTable(name = "shared_files", joinColumns =@JoinColumn(name = "user_id"), inverseJoinColumns=@JoinColumn(name="file_id"))
    private Set<NubeculaFile> sharedFiles;

    @ElementCollection
    @Singular
    @Cascade(value = {CascadeType.DELETE})
    @CollectionTable(name="user_roles", joinColumns=@JoinColumn(name="user_id"))
    private List<Role> roles;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public long getStorage() {
        return storage;
    }

    public void setStorage(long storage) {
        this.storage = storage;
    }

    public long getInStorage() {
        return inStorage;
    }

    public void setInStorage(long inStorage) {
        this.inStorage = inStorage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Set<NubeculaUser> getFriends() {
        return friends;
    }

    public void setFriends(Set<NubeculaUser> friends) {
        this.friends = friends;
    }

    public Set<NubeculaFile> getFiles() {
        return files;
    }

    public void setFiles(Set<NubeculaFile> files) {
        this.files = files;
    }

    public Set<NubeculaFile> getSharedFiles() {
        return sharedFiles;
    }

    public void setSharedFiles(Set<NubeculaFile> sharedFiles) {
        this.sharedFiles = sharedFiles;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }


    @Override
    public String toString() {
        return "NubeculaUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", registrationDate=" + registrationDate +
                ", storage=" + storage +
                ", inStorage=" + inStorage +
                ", description='" + description + '\'' +
                ", profilePicture=" + Arrays.toString(profilePicture) +
                ", friends=" + friends.toString() +
                ", files=" + files.toString() +
                ", sharedFiles=" + sharedFiles.toString() +
                ", roles=" + roles.toString() +
                '}';
    }
}
