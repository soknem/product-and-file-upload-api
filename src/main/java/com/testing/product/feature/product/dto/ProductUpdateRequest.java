package com.testing.product.feature.product.dto;

import jakarta.validation.constraints.Positive;

public record ProductUpdateRequest(

        String name,

        String image,

        @Positive(message = "price is positive")
        Double price,

        String description
) {
}
