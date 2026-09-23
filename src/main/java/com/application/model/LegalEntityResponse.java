package com.application.model;

public record LegalEntityResponse(
        Long id,
        String name,
        String inn,
        String ogrn
) {}