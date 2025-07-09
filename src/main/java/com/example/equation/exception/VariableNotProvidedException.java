package com.example.equation.exception;

/**
 * Exception thrown when a variable referenced in an expression is not provided
 * in the variable map during evaluation.
 */
public class VariableNotProvidedException extends RuntimeException {
    
    private final String variableName;
    
    /**
     * Creates a new exception for a missing variable.
     * 
     * @param variableName the name of the variable that was not provided
     */
    public VariableNotProvidedException(String variableName) {
        super("Variable '" + variableName + "' is not provided in the variable map");
        this.variableName = variableName;
    }
    
    /**
     * Creates a new exception with a custom message.
     * 
     * @param variableName the name of the variable that was not provided
     * @param message the custom error message
     */
    public VariableNotProvidedException(String variableName, String message) {
        super(message);
        this.variableName = variableName;
    }
    
    /**
     * Gets the name of the variable that was not provided.
     * 
     * @return the variable name
     */
    public String getVariableName() {
        return variableName;
    }
} 