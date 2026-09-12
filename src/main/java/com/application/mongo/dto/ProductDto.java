package com.application.mongo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductDto(
        String id,
        @NotBlank(message = "Product name is required") String name,
        @NotNull(message = "Category id is required") String categoryId,
        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity must be >= 0") Integer quantity
) {}