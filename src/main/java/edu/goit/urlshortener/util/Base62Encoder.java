package edu.goit.urlshortener.util;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class Base62Encoder {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALPHABET.length();
    private static final int SHORT_URL_LENGTH = 7;

    public String encode(long id) {
        StringBuilder shortURL = new StringBuilder();
        while (id > 0) {
            shortURL.append(ALPHABET.charAt((int) (id % BASE)));
            id /= BASE;
        }
        while (shortURL.length() < SHORT_URL_LENGTH) {
            shortURL.append(ALPHABET.charAt(new Random().nextInt(BASE)));
        }
        return shortURL.reverse().toString();
    }
}
