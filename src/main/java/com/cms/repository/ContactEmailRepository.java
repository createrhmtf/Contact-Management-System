package com.cms.repository;

import com.cms.model.entity.ContactEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactEmailRepository extends JpaRepository<ContactEmail, Long> {
    
    // Deletes all email records associated with a specific contact ID
    void deleteByContactId(Long contactId);
}