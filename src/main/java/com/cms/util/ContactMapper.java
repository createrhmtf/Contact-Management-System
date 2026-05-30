package com.cms.util;

import com.cms.model.dto.ContactDTO;
import com.cms.model.entity.Contact;
import com.cms.model.entity.ContactEmail;
import com.cms.model.entity.ContactPhone;
import com.cms.model.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ContactMapper converts between Contact entity objects (used by the database)
 * and ContactDTO objects (used by the API / outside world).
 *
 * Think of it as a translator: the database speaks "entity", the API speaks "DTO",
 * and this class translates between the two.
 */
@Component
public class ContactMapper {

    // =========================================================================
    // METHOD 1: toDTO — Convert a Contact entity → ContactDTO
    // =========================================================================

    /**
     * Converts a Contact entity (from the database) into a ContactDTO that
     * can be safely sent back to the client (API response).
     *
     * @param contact the Contact entity fetched from the database
     * @return a ContactDTO ready to be sent as a JSON response
     */
    public ContactDTO toDTO(Contact contact) {

        // Step 1: Convert the list of ContactEmail entities into a list of EmailDTOs.
        // If the emails list on the contact is null, we return an empty list instead
        // of throwing a NullPointerException.
        List<ContactDTO.EmailDTO> emailDTOs = contact.getEmails() == null
                ? Collections.emptyList()
                : contact.getEmails().stream()
                        .map(this::toEmailDTO)   // use the private helper below
                        .collect(Collectors.toList());

        // Step 2: Convert the list of ContactPhone entities into a list of PhoneDTOs.
        // Same null-safety pattern as emails above.
        List<ContactDTO.PhoneDTO> phoneDTOs = contact.getPhones() == null
                ? Collections.emptyList()
                : contact.getPhones().stream()
                        .map(this::toPhoneDTO)   // use the private helper below
                        .collect(Collectors.toList());

        // Step 3: Build and return the ContactDTO using the builder pattern.
        // Each field is copied from the Contact entity to the matching DTO field.
        return ContactDTO.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .title(contact.getTitle())
                .company(contact.getCompany())
                .address(contact.getAddress())
                .notes(contact.getNotes())
                .emails(emailDTOs)
                .phones(phoneDTOs)
                .build();
    }

    // ─── Private helper: map one ContactEmail entity → EmailDTO ───────────────

    /**
     * Maps a single ContactEmail entity to a ContactDTO.EmailDTO.
     * Called internally by toDTO() for each email in the list.
     */
    private ContactDTO.EmailDTO toEmailDTO(ContactEmail contactEmail) {
        // isPrimary is stored as Boolean (boxed) in the entity — we safely default
        // to false if it happens to be null, to keep the DTO's primitive boolean happy.
        return new ContactDTO.EmailDTO(
                contactEmail.getId(),
                contactEmail.getEmail(),
                contactEmail.getLabel(),
                Boolean.TRUE.equals(contactEmail.getIsPrimary())
        );
    }

    // ─── Private helper: map one ContactPhone entity → PhoneDTO ──────────────

    /**
     * Maps a single ContactPhone entity to a ContactDTO.PhoneDTO.
     * Called internally by toDTO() for each phone in the list.
     */
    private ContactDTO.PhoneDTO toPhoneDTO(ContactPhone contactPhone) {
        // Same null-safe isPrimary handling as toEmailDTO above.
        return new ContactDTO.PhoneDTO(
                contactPhone.getId(),
                contactPhone.getPhone(),
                contactPhone.getLabel(),
                Boolean.TRUE.equals(contactPhone.getIsPrimary())
        );
    }

    // =========================================================================
    // METHOD 2: toEntity — Convert a ContactDTO → Contact entity (+ set User)
    // =========================================================================

    /**
     * Converts a ContactDTO (received from the client in a POST/PUT request)
     * into a Contact entity that can be saved to the database.
     *
     * @param dto  the ContactDTO received from the API request body
     * @param user the authenticated User who owns this contact
     * @return a fully populated Contact entity ready to be saved
     */
    public Contact toEntity(ContactDTO dto, User user) {

        // Step 1: Build the Contact entity with all the simple (non-list) fields.
        // We do NOT set id here because it will be auto-generated by the database.
        // We also set createdAt manually here as a safe default; the @PrePersist
        // lifecycle hook in the Contact entity will also set it on first save.
        Contact contact = Contact.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .title(dto.getTitle())
                .company(dto.getCompany())
                .address(dto.getAddress())
                .notes(dto.getNotes())
                .user(user)                       // link this contact to the logged-in user
                .createdAt(LocalDateTime.now())   // record the creation timestamp
                .build();

        // Step 2: Convert each EmailDTO into a ContactEmail entity.
        // If the DTO's emails list is null, we use an empty list to avoid NPE.
        List<ContactEmail> emailEntities = new ArrayList<>();
        if (dto.getEmails() != null) {
            for (ContactDTO.EmailDTO emailDTO : dto.getEmails()) {

                // Build a ContactEmail entity from the DTO fields.
                ContactEmail contactEmail = ContactEmail.builder()
                        .email(emailDTO.getEmail())
                        .label(emailDTO.getLabel())
                        .isPrimary(emailDTO.isPrimary())
                        .contact(contact)   // set the back-reference to the parent Contact
                        .build();

                emailEntities.add(contactEmail);
            }
        }

        // Step 3: Convert each PhoneDTO into a ContactPhone entity.
        // Same null-safe pattern as emails above.
        List<ContactPhone> phoneEntities = new ArrayList<>();
        if (dto.getPhones() != null) {
            for (ContactDTO.PhoneDTO phoneDTO : dto.getPhones()) {

                // Build a ContactPhone entity from the DTO fields.
                ContactPhone contactPhone = ContactPhone.builder()
                        .phone(phoneDTO.getPhone())
                        .label(phoneDTO.getLabel())
                        .isPrimary(phoneDTO.isPrimary())
                        .contact(contact)   // set the back-reference to the parent Contact
                        .build();

                phoneEntities.add(contactPhone);
            }
        }

        // Step 4: Attach the fully-built email and phone lists to the Contact entity.
        // Because Contact uses CascadeType.ALL, saving the Contact will automatically
        // save all its emails and phones too — no separate save calls needed.
        contact.getEmails().addAll(emailEntities);
        contact.getPhones().addAll(phoneEntities);

        // Step 5: Return the complete Contact entity, ready to be passed to the repository.
        return contact;
    }
}
