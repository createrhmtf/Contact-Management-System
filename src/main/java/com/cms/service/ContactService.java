package com.cms.service;

import com.cms.model.dto.ContactDTO;
import org.springframework.data.domain.Page;

public interface ContactService {

    ContactDTO createContact(ContactDTO dto, String userEmail);

    ContactDTO updateContact(Long id, ContactDTO dto, String userEmail);

    void deleteContact(Long id, String userEmail);

    ContactDTO getContactById(Long id, String userEmail);

    Page<ContactDTO> getAllContacts(String userEmail, int page, int size);

    Page<ContactDTO> searchContacts(String userEmail, String keyword, int page, int size);
}
