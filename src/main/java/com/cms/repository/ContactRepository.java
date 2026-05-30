package com.cms.repository;

import com.cms.model.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    // Finds all contacts for a specific user, with pagination
    Page<Contact> findByUserId(Long userId, Pageable pageable);

    // Custom JPQL query to search by first or last name for a specific user
    // LOWER() is used to make the search case-insensitive
    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId AND " +
        "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Contact> searchContactsByKeyword(
        @Param("userId") Long userId, 
        @Param("keyword") String keyword, 
        Pageable pageable
    );
}