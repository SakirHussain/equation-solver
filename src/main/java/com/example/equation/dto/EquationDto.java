package com.example.equation.dto;

import com.example.equation.model.Token;

import java.util.List;


public class EquationDto {
    
    private Long id;
    private String infix;
    private List<Token> postfix;
    
    
    public EquationDto() {
    }
    
    
    public EquationDto(Long id, String infix, List<Token> postfix) {
        this.id = id;
        this.infix = infix;
        this.postfix = postfix;
    }
    
    
    public Long getId() {
        return id;
    }
    
    
    public void setId(Long id) {
        this.id = id;
    }
    
    
    public String getInfix() {
        return infix;
    }
    
    
    public void setInfix(String infix) {
        this.infix = infix;
    }
    
    
    public List<Token> getPostfix() {
        return postfix;
    }
    
    
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