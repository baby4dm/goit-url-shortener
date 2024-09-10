package edu.goit.urlshortener.exception;

public class MyEntityNotFoundException extends RuntimeException {

    public MyEntityNotFoundException(Long id) {
        super("Entity is not found, id="+id);
    }
}
