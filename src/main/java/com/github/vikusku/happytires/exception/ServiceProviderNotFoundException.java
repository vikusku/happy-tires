package com.github.vikusku.happytires.exception;

public class ServiceProviderNotFoundException extends RuntimeException {
    public ServiceProviderNotFoundException(String message) {
        super(message);
    }
}
