package com.amerd.schoolbook.exception;

import javax.validation.ValidationException;

public class UserExistsException extends ValidationException {
    public UserExistsException(String message) {
        super(message);
    }
}
