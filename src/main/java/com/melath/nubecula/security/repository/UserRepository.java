package com.melath.nubecula.security.repository;

import com.melath.nubecula.security.model.NubeculaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface UserRepository extends JpaRepository<NubeculaUser, Integer> {

    Optional<NubeculaUser> findByUsername(String name);
    Optional<NubeculaUser> findByEmail(String email);

    @Query(
        value="SELECT * FROM nubecula_user",
        nativeQuery=true
    )
    Stream<NubeculaUser> findAllUsers();
}
