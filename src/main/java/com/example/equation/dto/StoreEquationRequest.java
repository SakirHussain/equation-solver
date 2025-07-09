package com.example.equation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for storing a new equation.
 * Contains the mathematical expression to be parsed and stored.
 */
public class StoreEquationRequest {
    
    @NotBlank(message = "Equation cannot be blank")
    private String equation;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public StoreEquationRequest() {
    }
    
    /**
     * Creates a new store equation request.
     * 
     * @param equation the mathematical expression in infix notation
     */
    public StoreEquationRequest(String equation) {
        this.equation = equation;
    }
    
    /**
     * Gets the equation expression.
     * 
     * @return the mathematical expression
     */
    public String getEquation() {
        return equation;
    }
    
    /**
     * Sets the equation expression.
     * 
     * @param equation the mathematical expression
     */
    public void setEquation(String equation) {
        this.equation = equation;
    }
    
    @Override
    public String toString() {
        return String.format("StoreEquationRequest{equation='%s'}", equation);
    }
} 