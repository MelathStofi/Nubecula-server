package com.melath.nubecula.security.repository;

import com.melath.nubecula.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String name);
    Optional<User> findByEmail(String email);


}
