package com.example.equation.repository;

import com.example.equation.model.EquationEntity;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryEquationRepository implements EquationRepository {
    
   
    private final ConcurrentHashMap<Long, EquationEntity> storage = new ConcurrentHashMap<>();
    
    // get new id
    private final AtomicLong idGenerator = new AtomicLong(1);
    
   
    @Override
    public EquationEntity save(EquationEntity equation) {
        if (equation == null) {
            throw new IllegalArgumentException("Equation entity cannot be null");
        }
        
        EquationEntity entityToSave;
        
        if (equation.getId() == null) {
            Long newId = idGenerator.getAndIncrement();
            entityToSave = new EquationEntity(newId, equation.getInfix(), equation.getPostfix());
        } else {
            entityToSave = equation;
        }
        
        storage.put(entityToSave.getId(), entityToSave);
        return entityToSave;
    }
    
    
    @Override
    public Optional<EquationEntity> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        EquationEntity equation = storage.get(id);
        return Optional.ofNullable(equation);
    }
    

    @Override
    public List<EquationEntity> findAll() {
        return new ArrayList<>(storage.values());
    }
    
    
    public int size() {
        return storage.size();
    }
    
    
    public void clear() {
        storage.clear();
    }
} 