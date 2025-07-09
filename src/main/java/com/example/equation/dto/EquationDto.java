package com.example.equation.dto;

import com.example.equation.model.Token;

import java.util.List;

/**
 * Data Transfer Object for equation information.
 * Used to transfer equation data between service and presentation layers.
 */
public class EquationDto {
    
    private Long id;
    private String infix;
    private List<Token> postfix;
    
    /**
     * Default constructor for frameworks.
     */
    public EquationDto() {
    }
    
    /**
     * Creates a new EquationDto with all fields.
     * 
     * @param id the unique identifier
     * @param infix the infix expression string
     * @param postfix the postfix token list
     */
    public EquationDto(Long id, String infix, List<Token> postfix) {
        this.id = id;
        this.infix = infix;
        this.postfix = postfix;
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
    
    /**
     * Gets the postfix token list.
     * 
     * @return the postfix tokens
     */
    public List<Token> getPostfix() {
        return postfix;
    }
    
    /**
     * Sets the postfix token list.
     * 
     * @param postfix the postfix tokens
     */
    public void setPostfix(List<Token> postfix) {
        this.postfix = postfix;
    }
    
    @Override
    public String toString() {
        return String.format("EquationDto{id=%d, infix='%s', postfix=%s}", 
            id, infix, postfix);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EquationDto that = (EquationDto) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 