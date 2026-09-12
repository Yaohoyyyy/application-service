package com.application.mongo.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryDto(
        String id,
        @NotBlank(message = "Category name is required") String name
) {}