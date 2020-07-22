package com.melath.nubecula.user.model.entity;

import com.melath.nubecula.storage.model.entity.NubeculaFile;
import lombok.*;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "user")
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

    private UUID rootDirectoryId;

    private UUID trashBinId;

}
