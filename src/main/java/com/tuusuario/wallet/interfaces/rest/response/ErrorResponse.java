package com.tuusuario.wallet.interfaces.rest.response;

import java.time.Instant;
import java.util.Map;

/**
 * DTO uniforme de errores HTTP.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> details
) {
}

