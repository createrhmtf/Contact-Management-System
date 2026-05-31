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

@Component
public class ContactMapper {

    public ContactDTO toDTO(Contact contact) {
        List<ContactDTO.EmailDTO> emailDTOs = contact.getEmails() == null
                ? Collections.emptyList()
                : contact.getEmails().stream()
                        .map(this::toEmailDTO)
                        .collect(Collectors.toList());

        List<ContactDTO.PhoneDTO> phoneDTOs = contact.getPhones() == null
                ? Collections.emptyList()
                : contact.getPhones().stream()
                        .map(this::toPhoneDTO)
                        .collect(Collectors.toList());

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

    private ContactDTO.EmailDTO toEmailDTO(ContactEmail contactEmail) {
        return new ContactDTO.EmailDTO(
                contactEmail.getId(),
                contactEmail.getEmail(),
                contactEmail.getLabel(),
                Boolean.TRUE.equals(contactEmail.getIsPrimary()));
    }

    private ContactDTO.PhoneDTO toPhoneDTO(ContactPhone contactPhone) {
        return new ContactDTO.PhoneDTO(
                contactPhone.getId(),
                contactPhone.getPhone(),
                contactPhone.getLabel(),
                Boolean.TRUE.equals(contactPhone.getIsPrimary()));
    }

    public Contact toEntity(ContactDTO dto, User user) {
        Contact contact = Contact.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .title(dto.getTitle())
                .company(dto.getCompany())
                .address(dto.getAddress())
                .notes(dto.getNotes())
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        List<ContactEmail> emailEntities = new ArrayList<>();
        if (dto.getEmails() != null) {
            for (ContactDTO.EmailDTO emailDTO : dto.getEmails()) {
                emailEntities.add(ContactEmail.builder()
                        .email(emailDTO.getEmail())
                        .label(emailDTO.getLabel())
                        .isPrimary(emailDTO.isPrimary())
                        .contact(contact)
                        .build());
            }
        }

        List<ContactPhone> phoneEntities = new ArrayList<>();
        if (dto.getPhones() != null) {
            for (ContactDTO.PhoneDTO phoneDTO : dto.getPhones()) {
                phoneEntities.add(ContactPhone.builder()
                        .phone(phoneDTO.getPhone())
                        .label(phoneDTO.getLabel())
                        .isPrimary(phoneDTO.isPrimary())
                        .contact(contact)
                        .build());
            }
        }

        contact.getEmails().addAll(emailEntities);
        contact.getPhones().addAll(phoneEntities);
        return contact;
    }
}
