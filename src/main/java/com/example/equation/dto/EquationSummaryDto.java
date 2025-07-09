package com.example.equation.dto;

/**
 * Simplified DTO for listing equations.
 * Contains only the essential information: ID and infix expression.
 */
public class EquationSummaryDto {
    
    private Long id;
    private String infix;
    
    /**
     * Default constructor for frameworks.
     */
    public EquationSummaryDto() {
    }
    
    /**
     * Creates a new equation summary DTO.
     * 
     * @param id the unique identifier
     * @param infix the infix expression string
     */
    public EquationSummaryDto(Long id, String infix) {
        this.id = id;
        this.infix = infix;
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
    
    /**
     * Gets the infix expression.
     * 
     * @return the infix expression string
     */
    public String getInfix() {
        return infix;
    }
    
    /**
     * Sets the infix expression.
     * 
     * @param infix the infix expression string
     */
    public void setInfix(String infix) {
        this.infix = infix;
    }
    
    @Override
    public String toString() {
        return String.format("EquationSummaryDto{id=%d, infix='%s'}", id, infix);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EquationSummaryDto that = (EquationSummaryDto) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 