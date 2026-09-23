package com.application.model;

import jakarta.validation.constraints.NotBlank;

public record LegalEntityRequest(
        @NotBlank(message = "INN is required")
        String inn,
        @NotBlank(message = "OGRN is required")
        String ogrn
) {}