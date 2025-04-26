package com.polarbookshop.catalogservice.exception;

public class BookAlreadyExistsException extends RuntimeException {

    public BookAlreadyExistsException(String message) {
        super("A book with ISBN " + message + " already exists");
    }
}
