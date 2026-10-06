package com.application.model;

import com.application.entity.Vendor;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record VendorDto(
        Long id,
        @NotBlank(message = "Name is required") String name,
        boolean active
) implements Serializable {
    public static VendorDto fromEntity(Vendor vendor) {
        return new VendorDto(vendor.getId(), vendor.getName(), vendor.isActive());
    }
}