package com.cms.controller;

import com.cms.model.dto.ContactDTO;
import com.cms.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@Slf4j
public class ContactController {

    private final ContactService contactService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    public ResponseEntity<Page<ContactDTO>> getAllContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userEmail = getCurrentUserEmail();
        log.info("Get all contacts for: {}", userEmail);
        return ResponseEntity.ok(contactService.getAllContacts(userEmail, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ContactDTO>> searchContacts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userEmail = getCurrentUserEmail();
        log.info("Search contacts keyword: {} for: {}", keyword, userEmail);
        return ResponseEntity.ok(contactService.searchContacts(userEmail, keyword, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactDTO> getContactById(@PathVariable Long id) {
        String userEmail = getCurrentUserEmail();
        return ResponseEntity.ok(contactService.getContactById(id, userEmail));
    }

    @PostMapping
    public ResponseEntity<ContactDTO> createContact(@Valid @RequestBody ContactDTO contactDTO) {
        String userEmail = getCurrentUserEmail();
        log.info("Creating contact for: {}", userEmail);
        ContactDTO createdContact = contactService.createContact(contactDTO, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdContact);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactDTO> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactDTO contactDTO) {
        String userEmail = getCurrentUserEmail();
        log.info("Updating contact: {} for: {}", id, userEmail);
        return ResponseEntity.ok(contactService.updateContact(id, contactDTO, userEmail));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        String userEmail = getCurrentUserEmail();
        log.info("Deleting contact: {} by: {}", id, userEmail);
        contactService.deleteContact(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}
