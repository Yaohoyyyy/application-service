package com.application.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMessage {
    private String firstName;
    private String lastName;
    private String email;
}