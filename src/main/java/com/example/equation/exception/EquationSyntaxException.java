package com.example.equation.exception;


public class EquationSyntaxException extends RuntimeException {
    
    
    public EquationSyntaxException(String message) {
        super(message);
    }
    
    
    public EquationSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
} 