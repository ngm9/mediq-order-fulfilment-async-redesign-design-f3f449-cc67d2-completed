package com.medix.orders.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CheckoutRequest(
        @NotBlank String customerId,
        @NotBlank @Size(min = 2, max = 2) String country,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String currency) {
}
