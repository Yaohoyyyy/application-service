package com.application.model;

import com.application.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDto(
        Long id,
        @NotBlank(message = "Last name is required") String lastName,
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        String createdAt,
        String updatedAt
) {
    public static UserDto fromEntity(User user) {
        return new UserDto(
                user.getId(),
                user.getLastName(),
                user.getFirstName(),
                user.getEmail(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
                user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null
        );
    }

    public User toEntity() {
        User user = new User();
        user.setLastName(this.lastName);
        user.setFirstName(this.firstName);
        user.setEmail(this.email);
        return user;
    }

}
