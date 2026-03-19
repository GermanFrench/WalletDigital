package com.tuusuario.wallet.application.service;

/**
 * Excepción para recursos de aplicación que no existen.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

