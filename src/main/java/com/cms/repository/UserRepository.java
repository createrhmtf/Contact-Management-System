package com.cms.repository;

import com.cms.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Optional is used here because a user might not be found
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    // Returns true if the email exists in the database
    boolean existsByEmail(String email);
    
    // Returns true if the phone number exists in the database
    boolean existsByPhoneNumber(String phoneNumber);
}