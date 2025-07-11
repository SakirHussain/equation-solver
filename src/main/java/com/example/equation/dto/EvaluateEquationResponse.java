package com.example.equation.dto;


public class EvaluateEquationResponse {
    
    private double result;
    
    
    public EvaluateEquationResponse() {
    }
    
    
    public EvaluateEquationResponse(double result) {
        this.result = result;
    }
    
    
    public double getResult() {
        return result;
    }
    
    
    public void setResult(double result) {
        this.result = result;
    }
    
    @Override
    public String toString() {
        return String.format("EvaluateEquationResponse{result=%.6f}", result);
    }
} 