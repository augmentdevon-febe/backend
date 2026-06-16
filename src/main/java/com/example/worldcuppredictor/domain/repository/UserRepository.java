package com.example.worldcuppredictor.domain.repository;

import com.example.worldcuppredictor.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGoogleSubject(String googleSubject);
    Optional<User> findByEmail(String email);
}
