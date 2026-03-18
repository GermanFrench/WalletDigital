package com.tuusuario.wallet.interfaces.rest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request HTTP para crear una cuenta.
 */
public record CreateAccountRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe ser válido")
        @Size(max = 150, message = "El email no puede superar los 150 caracteres")
        String email,

        @NotBlank(message = "La moneda es obligatoria")
        @Pattern(regexp = "^[A-Z]{3}$", message = "La moneda debe tener 3 letras en mayúsculas")
        String currency
) {
}

