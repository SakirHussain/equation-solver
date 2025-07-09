package com.example.equation.exception;

/**
 * Exception thrown when an equation contains invalid syntax or cannot be parsed.
 */
public class EquationSyntaxException extends RuntimeException {
    
    /**
     * Creates a new exception with the specified message.
     * 
     * @param message the error message describing the syntax problem
     */
    public EquationSyntaxException(String message) {
        super(message);
    }
    
    /**
     * Creates a new exception with the specified message and cause.
     * 
     * @param message the error message describing the syntax problem
     * @param cause the underlying cause of the exception
     */
    public EquationSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
} 