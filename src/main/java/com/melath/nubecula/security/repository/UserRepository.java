package com.melath.nubecula.security.repository;

import com.melath.nubecula.security.model.NubeculaUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<NubeculaUser, Integer> {

    Optional<NubeculaUser> findByUsername(String name);
    Optional<NubeculaUser> findByEmail(String email);


}
