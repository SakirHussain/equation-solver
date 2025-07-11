package com.example.equation.dto;


public class StoreEquationResponse {
    
    private Long id;
    
    
    public StoreEquationResponse() {
    }
    
    
    public StoreEquationResponse(Long id) {
        this.id = id;
    }
    
    
    public Long getId() {
        return id;
    }
    
    
    public void setId(Long id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return String.format("StoreEquationResponse{id=%d}", id);
    }
} 