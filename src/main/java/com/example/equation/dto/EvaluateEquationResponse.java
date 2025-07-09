package com.example.equation.dto;

/**
 * Response DTO for equation evaluation operations.
 * Contains the computed result of the mathematical expression.
 */
public class EvaluateEquationResponse {
    
    private double result;
    
    /**
     * Default constructor for JSON serialization.
     */
    public EvaluateEquationResponse() {
    }
    
    /**
     * Creates a new evaluate equation response.
     * 
     * @param result the computed result of the equation
     */
    public EvaluateEquationResponse(double result) {
        this.result = result;
    }
    
    /**
     * Gets the evaluation result.
     * 
     * @return the computed result
     */
    public double getResult() {
        return result;
    }
    
    /**
     * Sets the evaluation result.
     * 
     * @param result the computed result
     */
    public void setResult(double result) {
        this.result = result;
    }
    
    @Override
    public String toString() {
        return String.format("EvaluateEquationResponse{result=%.6f}", result);
    }
} 