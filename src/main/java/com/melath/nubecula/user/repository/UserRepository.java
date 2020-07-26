package com.melath.nubecula.user.repository;

import com.melath.nubecula.user.model.entity.NubeculaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.stream.Stream;

public interface UserRepository extends JpaRepository<NubeculaUser, Integer> {

    Optional<NubeculaUser> findByUsername(String name);
    Optional<NubeculaUser> findByEmail(String email);

    NubeculaUser findFirstByUsername(String username);

    @Query(
        value="SELECT * FROM user",
        nativeQuery=true
    )
    Stream<NubeculaUser> findAllUsers();

    @Query(value="SELECT u FROM user u WHERE u.username LIKE :searched% ORDER BY u.username")
    Stream<NubeculaUser> searchUsersBeginning(@Param("searched") String searched);

    @Query(value="SELECT u FROM user u WHERE u.username LIKE %:searched% ORDER BY u.username")
    Stream<NubeculaUser> searchUsersAnywhere(@Param("searched") String searched);


}
