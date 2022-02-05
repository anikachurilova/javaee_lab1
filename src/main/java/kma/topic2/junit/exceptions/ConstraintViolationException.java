package kma.topic2.junit.exceptions;

import lombok.Getter;

import java.util.List;

public class ConstraintViolationException extends RuntimeException {

    @Getter
    private final List<String> errors;

    public ConstraintViolationException(final List<String> errors) {
        super("You have errors in you object");
        this.errors = errors;
    }

}
