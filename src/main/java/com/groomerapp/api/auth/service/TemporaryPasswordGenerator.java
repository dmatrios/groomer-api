package com.groomerapp.api.auth.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TemporaryPasswordGenerator {

    private static final String CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ" +   // sin I, O
                    "abcdefghijkmnopqrstuvwxyz" +   // sin l
                    "23456789";                     // sin 0,1

    private static final int DEFAULT_LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        return generate(DEFAULT_LENGTH);
    }

    public String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
