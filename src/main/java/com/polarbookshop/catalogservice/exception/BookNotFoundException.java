package com.polarbookshop.catalogservice.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String message) {
        super("The book with ISBN " + message + " not found.");
    }
}
