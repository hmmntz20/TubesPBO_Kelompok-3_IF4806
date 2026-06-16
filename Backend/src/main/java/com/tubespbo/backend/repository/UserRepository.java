package com.tubespbo.backend.repository;

import com.tubespbo.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Cukup dengan mengetikkan nama method ini, Spring Boot otomatis paham 
    // bahwa kita ingin mencari User berdasarkan kolom email. Canggih, kan?
    Optional<User> findByEmail(String email);
    
    Optional<User> findByVerificationToken(String verificationToken);
}