package com.evote.app.citizen_management.exceptions;

public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException() {
        super("User with email  already exists.");
    }
}
