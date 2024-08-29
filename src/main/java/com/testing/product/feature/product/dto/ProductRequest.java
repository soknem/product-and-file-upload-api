package com.testing.product.feature.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductRequest(

        @NotBlank(message = "name is require")
        String name,

        String image,

        @NotNull(message = "price is require")
        @Positive(message = "price is positive")
        Double price,

        String description
) {
}
