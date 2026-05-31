package com.cms.service.impl;

import com.cms.exception.AccessDeniedException;
import com.cms.exception.ResourceNotFoundException;
import com.cms.model.dto.ContactDTO;
import com.cms.model.entity.Contact;
import com.cms.model.entity.ContactEmail;
import com.cms.model.entity.ContactPhone;
import com.cms.model.entity.User;
import com.cms.repository.ContactRepository;
import com.cms.repository.UserRepository;
import com.cms.service.ContactService;
import com.cms.util.ContactMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final ContactMapper contactMapper;

    @Override
    public ContactDTO createContact(ContactDTO dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Contact contact = contactMapper.toEntity(dto, user);
        Contact savedContact = contactRepository.save(contact);
        log.info("Contact created for user: {}", userEmail);
        return contactMapper.toDTO(savedContact);
    }

    @Override
    public ContactDTO updateContact(Long id, ContactDTO dto, String userEmail) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));

        if (!contact.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You do not have permission to update this contact");
        }

        contact.setFirstName(dto.getFirstName());
        contact.setLastName(dto.getLastName());
        contact.setTitle(dto.getTitle());
        contact.setCompany(dto.getCompany());
        contact.setAddress(dto.getAddress());
        contact.setNotes(dto.getNotes());

        contact.getEmails().clear();
        if (dto.getEmails() != null) {
            List<ContactEmail> updatedEmails = new ArrayList<>();
            for (ContactDTO.EmailDTO emailDTO : dto.getEmails()) {
                updatedEmails.add(ContactEmail.builder()
                        .email(emailDTO.getEmail())
                        .label(emailDTO.getLabel())
                        .isPrimary(emailDTO.isPrimary())
                        .contact(contact)
                        .build());
            }
            contact.getEmails().addAll(updatedEmails);
        }

        contact.getPhones().clear();
        if (dto.getPhones() != null) {
            List<ContactPhone> updatedPhones = new ArrayList<>();
            for (ContactDTO.PhoneDTO phoneDTO : dto.getPhones()) {
                updatedPhones.add(ContactPhone.builder()
                        .phone(phoneDTO.getPhone())
                        .label(phoneDTO.getLabel())
                        .isPrimary(phoneDTO.isPrimary())
                        .contact(contact)
                        .build());
            }
            contact.getPhones().addAll(updatedPhones);
        }

        Contact savedContact = contactRepository.save(contact);
        return contactMapper.toDTO(savedContact);
    }

    @Override
    public void deleteContact(Long id, String userEmail) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));

        if (!contact.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You do not have permission to delete this contact");
        }

        contactRepository.delete(contact);
        log.info("Contact deleted: {} by user: {}", id, userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactDTO getContactById(Long id, String userEmail) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));

        if (!contact.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You do not have permission to view this contact");
        }

        return contactMapper.toDTO(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactDTO> getAllContacts(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
        Page<Contact> contacts = contactRepository.findByUserId(user.getId(), pageable);
        return contacts.map(contactMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactDTO> searchContacts(String userEmail, String keyword, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Pageable pageable = PageRequest.of(page, size);
        Page<Contact> results = contactRepository.searchContactsByKeyword(
                user.getId(), keyword, pageable);
        return results.map(contactMapper::toDTO);
    }
}
