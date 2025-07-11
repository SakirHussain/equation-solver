package com.example.equation.dto;


public class EquationSummaryDto {
    
    private Long id;
    private String infix;
    
    
    public EquationSummaryDto() {
    }
    
    
    public EquationSummaryDto(Long id, String infix) {
        this.id = id;
        this.infix = infix;
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