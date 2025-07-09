package com.example.equation.repository;

import com.example.equation.model.EquationEntity;

import java.util.List;
import java.util.Optional;

public interface EquationRepository {
    
    EquationEntity save(EquationEntity equation);
    
    Optional<EquationEntity> findById(Long id);
    
    List<EquationEntity> findAll();
} 