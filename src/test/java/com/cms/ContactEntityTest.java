package com.cms;

import com.cms.model.entity.Contact;
import com.cms.model.entity.ContactEmail;
import com.cms.model.entity.ContactPhone;
import com.cms.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Contact Entity Tests")
class ContactEntityTest {

    private Contact contact;
    private User user;

    @BeforeEach
    void setUp() {
        // Create a test User
        user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .passwordHash("testPassword")
                .build();

        // Create a test Contact
        contact = Contact.builder()
                .firstName("John")
                .lastName("Doe")
                .address("123 Main Street")
                .user(user)
                .emails(new ArrayList<>())
                .phones(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should create a Contact with builder pattern and set basic fields")
    void testContactBuilderCreation() {
        // Assert: Verify Contact fields are set correctly
        assertNotNull(contact, "Contact object should not be null");
        assertEquals("John", contact.getFirstName(), "First name should match");
        assertEquals("Doe", contact.getLastName(), "Last name should match");
        assertEquals("123 Main Street", contact.getAddress(), "Address should match");
        assertNotNull(contact.getUser(), "Contact should be associated with a User");
        assertEquals("Test", contact.getUser().getFirstName(), "User first name should match");
    }

    @Test
    @DisplayName("Should add a single ContactEmail to Contact's email list")
    void testAddSingleContactEmailToContact() {
        // Arrange: Create a ContactEmail
        ContactEmail email = ContactEmail.builder()
                .email("john.doe@company.com")
                .label("work")
                .contact(contact)
                .build();

        // Act: Add email to contact's list
        contact.getEmails().add(email);

        // Assert: Verify email was added
        assertEquals(1, contact.getEmails().size(), "Contact should have 1 email");
        assertEquals("john.doe@company.com", contact.getEmails().get(0).getEmail(), "Email address should match");
        assertEquals("work", contact.getEmails().get(0).getLabel(), "Email label should match");
    }

    @Test
    @DisplayName("Should add multiple ContactEmails to Contact's email list")
    void testAddMultipleContactEmailsToContact() {
        // Arrange: Create multiple emails
        ContactEmail workEmail = ContactEmail.builder()
                .email("john@company.com")
                .label("work")
                .contact(contact)
                .build();

        ContactEmail personalEmail = ContactEmail.builder()
                .email("john.personal@gmail.com")
                .label("personal")
                .contact(contact)
                .build();

        // Act: Add both emails to contact
        contact.getEmails().add(workEmail);
        contact.getEmails().add(personalEmail);

        // Assert: Verify both emails were added
        assertEquals(2, contact.getEmails().size(), "Contact should have 2 emails");
        assertTrue(contact.getEmails().stream()
                .anyMatch(e -> e.getLabel().equals("work")), "Contact should have work email");
        assertTrue(contact.getEmails().stream()
                .anyMatch(e -> e.getLabel().equals("personal")), "Contact should have personal email");
    }

    @Test
    @DisplayName("Should add a single ContactPhone to Contact's phone list")
    void testAddSingleContactPhoneToContact() {
        // Arrange: Create a ContactPhone
        ContactPhone phone = ContactPhone.builder()
                .phone("555-1234")
                .label("mobile")
                .contact(contact)
                .build();

        // Act: Add phone to contact's list
        contact.getPhones().add(phone);

        // Assert: Verify phone was added
        assertEquals(1, contact.getPhones().size(), "Contact should have 1 phone");
        assertEquals("555-1234", contact.getPhones().get(0).getPhone(), "Phone number should match");
        assertEquals("mobile", contact.getPhones().get(0).getLabel(), "Phone label should match");
    }

    @Test
    @DisplayName("Should add multiple ContactPhones to Contact's phone list")
    void testAddMultipleContactPhonesToContact() {
        // Arrange: Create multiple phones
        ContactPhone mobilePhone = ContactPhone.builder()
                .phone("555-1111")
                .label("mobile")
                .contact(contact)
                .build();

        ContactPhone homePhone = ContactPhone.builder()
                .phone("555-2222")
                .label("home")
                .contact(contact)
                .build();

        // Act: Add both phones to contact
        contact.getPhones().add(mobilePhone);
        contact.getPhones().add(homePhone);

        // Assert: Verify both phones were added
        assertEquals(2, contact.getPhones().size(), "Contact should have 2 phones");
        assertTrue(contact.getPhones().stream()
                .anyMatch(p -> p.getLabel().equals("mobile")), "Contact should have mobile phone");
        assertTrue(contact.getPhones().stream()
                .anyMatch(p -> p.getLabel().equals("home")), "Contact should have home phone");
    }

    @Test
    @DisplayName("Should add both emails and phones to the same Contact")
    void testAddEmailsAndPhonesToContact() {
        // Arrange: Create emails and phones
        ContactEmail email = ContactEmail.builder()
                .email("john@example.com")
                .label("work")
                .contact(contact)
                .build();

        ContactPhone phone = ContactPhone.builder()
                .phone("555-9999")
                .label("mobile")
                .contact(contact)
                .build();

        // Act: Add both email and phone
        contact.getEmails().add(email);
        contact.getPhones().add(phone);

        // Assert: Verify both were added
        assertEquals(1, contact.getEmails().size(), "Contact should have 1 email");
        assertEquals(1, contact.getPhones().size(), "Contact should have 1 phone");
        assertNotNull(contact.getEmails().get(0), "Email should not be null");
        assertNotNull(contact.getPhones().get(0), "Phone should not be null");
    }

    @Test
    @DisplayName("Should remove an email from Contact's email list")
    void testRemoveContactEmailFromContact() {
        // Arrange: Add an email
        ContactEmail email = ContactEmail.builder()
                .email("temp@example.com")
                .label("temporary")
                .contact(contact)
                .build();
        contact.getEmails().add(email);
        assertEquals(1, contact.getEmails().size(), "Contact should have 1 email before removal");

        // Act: Remove the email
        contact.getEmails().remove(0);

        // Assert: Verify email was removed
        assertEquals(0, contact.getEmails().size(), "Contact should have 0 emails after removal");
    }
}
