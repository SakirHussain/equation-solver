package com.example.equation.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;


public class EvaluateEquationRequest {
    
    @NotEmpty(message = "Variables map cannot be empty")
    private Map<String, Double> variables;
    
    
    public EvaluateEquationRequest() {
    }
    
    
    public EvaluateEquationRequest(Map<String, Double> variables) {
        this.variables = variables;
    }
    
    
    public Map<String, Double> getVariables() {
        return variables;
    }
    
    
    public void setVariables(Map<String, Double> variables) {
        this.variables = variables;
    }
    
    @Override
    public String toString() {
        return String.format("EvaluateEquationRequest{variables=%s}", variables);
    }
} 