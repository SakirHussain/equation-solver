package com.example.equation.dto;

/**
 * Response DTO for storing equation operations.
 * Contains the unique ID of the newly stored equation.
 */
public class StoreEquationResponse {
    
    private Long id;
    
    /**
     * Default constructor for JSON serialization.
     */
    public StoreEquationResponse() {
    }
    
    /**
     * Creates a new store equation response.
     * 
     * @param id the unique identifier of the stored equation
     */
    public StoreEquationResponse(Long id) {
        this.id = id;
    }
    
    /**
     * Gets the equation ID.
     * 
     * @return the unique identifier
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Sets the equation ID.
     * 
     * @param id the unique identifier
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return String.format("StoreEquationResponse{id=%d}", id);
    }
} 