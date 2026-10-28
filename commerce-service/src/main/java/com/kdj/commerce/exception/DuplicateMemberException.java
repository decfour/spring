package com.kdj.commerce.exception;

public class DuplicateMemberException extends BusinessConflictException {
    public enum Field { EMAIL, SIGN_IN_ID }

    private final Field field;

    public DuplicateMemberException(Field field, String message) {
        super(message);
        this.field = field;
    }

    public Field getField() {
        return field;
    }
}
