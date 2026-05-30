package com.cms.service;

import com.cms.model.dto.ContactDTO;
import org.springframework.data.domain.Page;

/**
 * ContactService defines the operations available for managing contacts.
 *
 * Think of this as a "menu" of what the system can do with contacts.
 * The actual implementation (how each method works) lives in ContactServiceImpl.
 *
 * Using an interface here is good practice — it keeps the "what" separate
 * from the "how", making the code easier to test and maintain.
 */
public interface ContactService {

    /**
     * Create a brand-new contact and link it to the logged-in user.
     *
     * @param dto       the contact data submitted by the user
     * @param userEmail the email of the currently logged-in user (from JWT)
     * @return the saved contact as a DTO (includes the generated database ID)
     */
    ContactDTO createContact(ContactDTO dto, String userEmail);

    /**
     * Update an existing contact's details.
     * The caller must own the contact — you cannot edit someone else's contact.
     *
     * @param id        the database ID of the contact to update
     * @param dto       the new contact data to apply
     * @param userEmail the email of the currently logged-in user (from JWT)
     * @return the updated contact as a DTO
     */
    ContactDTO updateContact(Long id, ContactDTO dto, String userEmail);

    /**
     * Permanently delete a contact from the database.
     * The caller must own the contact.
     *
     * @param id        the database ID of the contact to delete
     * @param userEmail the email of the currently logged-in user (from JWT)
     */
    void deleteContact(Long id, String userEmail);

    /**
     * Fetch a single contact by its ID.
     * The caller must own the contact.
     *
     * @param id        the database ID of the contact
     * @param userEmail the email of the currently logged-in user (from JWT)
     * @return the contact as a DTO
     */
    ContactDTO getContactById(Long id, String userEmail);

    /**
     * Fetch a paginated list of ALL contacts belonging to the logged-in user,
     * sorted alphabetically by first name.
     *
     * @param userEmail the email of the currently logged-in user (from JWT)
     * @param page      which page to fetch (0-based: 0 = first page)
     * @param size      how many contacts to return per page
     * @return a Page of ContactDTOs
     */
    Page<ContactDTO> getAllContacts(String userEmail, int page, int size);

    /**
     * Search through the logged-in user's contacts by a keyword.
     * The search checks first name and last name (case-insensitive).
     *
     * @param userEmail the email of the currently logged-in user (from JWT)
     * @param keyword   the search term (e.g. "john" or "smith")
     * @param page      which page to fetch (0-based)
     * @param size      how many results per page
     * @return a Page of matching ContactDTOs
     */
    Page<ContactDTO> searchContacts(String userEmail, String keyword, int page, int size);
}
