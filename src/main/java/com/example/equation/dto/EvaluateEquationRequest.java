package com.example.equation.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

/**
 * Request DTO for evaluating an equation with variable values.
 * Contains the variable name-value mappings needed for evaluation.
 */
public class EvaluateEquationRequest {
    
    @NotEmpty(message = "Variables map cannot be empty")
    private Map<String, Double> variables;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public EvaluateEquationRequest() {
    }
    
    /**
     * Creates a new evaluate equation request.
     * 
     * @param variables map of variable names to their values
     */
    public EvaluateEquationRequest(Map<String, Double> variables) {
        this.variables = variables;
    }
    
    /**
     * Gets the variables map.
     * 
     * @return map of variable names to values
     */
    public Map<String, Double> getVariables() {
        return variables;
    }
    
    /**
     * Sets the variables map.
     * 
     * @param variables map of variable names to values
     */
    public void setVariables(Map<String, Double> variables) {
        this.variables = variables;
    }
    
    @Override
    public String toString() {
        return String.format("EvaluateEquationRequest{variables=%s}", variables);
    }
} 