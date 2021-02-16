package com.upc.gessi.qrapids.app.domain.exceptions;

public class CategoriesException extends Exception {

    public CategoriesException() {}

    // Constructor that accepts a message
    public CategoriesException(String message)
    {
        super(message);
    }

}
