package com.cms.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactDTO {

    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;
    private String title;
    private String company;
    private String address;
    private String notes;
    private List<EmailDTO> emails;
    private List<PhoneDTO> phones;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailDTO {

        private Long id;

        @Email(message = "Email must be valid")
        private String email;

        private String label;
        private boolean isPrimary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhoneDTO {

        private Long id;
        private String phone;
        private String label;
        private boolean isPrimary;
    }
}
