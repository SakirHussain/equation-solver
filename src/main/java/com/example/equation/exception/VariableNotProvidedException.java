package com.example.equation.exception;


public class VariableNotProvidedException extends RuntimeException {
    
    private final String variableName;
    
    
    public VariableNotProvidedException(String variableName) {
        super("Variable '" + variableName + "' is not provided in the variable map");
        this.variableName = variableName;
    }
    
    
    public VariableNotProvidedException(String variableName, String message) {
        super(message);
        this.variableName = variableName;
    }
    
    
    public String getVariableName() {
        return variableName;
    }
} 