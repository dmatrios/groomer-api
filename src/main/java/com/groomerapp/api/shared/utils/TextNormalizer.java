package com.groomerapp.api.shared.utils;

public final class TextNormalizer {

    private TextNormalizer() {}

    public static String normalize(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        String collapsedSpaces = trimmed.replaceAll("\\s+", " ");
        return collapsedSpaces.toLowerCase();
    }
}
