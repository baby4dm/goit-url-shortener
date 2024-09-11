package edu.goit.urlshortener.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MyEntityNotFoundException extends RuntimeException {

    public MyEntityNotFoundException(String url) {
        super(String.format("Entity is not found %s", url));
    }
}
