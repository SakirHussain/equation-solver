package com.example.equation.service;

import com.example.equation.dto.EquationDto;
import com.example.equation.dto.EquationSummaryDto;
import com.example.equation.model.EquationEntity;
import com.example.equation.model.Node;
import com.example.equation.model.Token;
import com.example.equation.model.TokenValue;
import com.example.equation.repository.EquationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class EquationService {
    
    private final EquationRepository equationRepository;
    private final ParserService parserService;
    private final EvaluationService evaluationService;
    
    
    public EquationService(EquationRepository equationRepository, 
                          ParserService parserService, 
                          EvaluationService evaluationService) {
        this.equationRepository = equationRepository;
        this.parserService = parserService;
        this.evaluationService = evaluationService;
    }
    
    
    public Long storeEquation(String infix) {
        if (infix == null || infix.trim().isEmpty()) {
            throw new IllegalArgumentException("Infix expression cannot be null or empty");
        }
        
        String trimmedInfix = infix.trim();
        
        Node expressionTree = parserService.parseExpression(trimmedInfix);
        // compute hash
        String astHash = expressionTree.generateHash();
        
        // check if equation already exists
        Optional<EquationEntity> existingEquation = equationRepository.findByAstHash(astHash);
        if (existingEquation.isPresent()) {
            return existingEquation.get().getId();
        }
        
        //else
        List<TokenValue> infixTokens = parserService.tokenize(trimmedInfix);
        List<TokenValue> postfixTokens = parserService.infixToPostfix(infixTokens);
        
        List<Token> postfixTypes = postfixTokens.stream()
            .map(TokenValue::getType)
            .toList();
        
        EquationEntity equation = new EquationEntity(null, trimmedInfix, postfixTypes, astHash);
        EquationEntity savedEquation = equationRepository.save(equation);
        
        return savedEquation.getId();
    }
    
    
    public List<EquationDto> getAllEquations() {
        List<EquationEntity> entities = equationRepository.findAll();
        
        return entities.stream()
            .map(this::convertToDto)
            .toList();
    }
    
    
    public List<EquationSummaryDto> getAllEquationSummaries() {
        List<EquationEntity> entities = equationRepository.findAll();
        
        return entities.stream()
            .map(this::convertToSummaryDto)
            .toList();
    }
    
    
    public double evaluateEquation(Long id, Map<String, Double> variables) {
        if (id == null) {
            throw new IllegalArgumentException("Equation ID cannot be null");
        }
        if (variables == null) {
            throw new IllegalArgumentException("Variables map cannot be null");
        }
        
        Optional<EquationEntity> equationOpt = equationRepository.findById(id);
        if (equationOpt.isEmpty()) {
            throw new IllegalStateException("Equation with ID " + id + " not found");
        }
        
        EquationEntity equation = equationOpt.get();
        
        Node expressionTree = parserService.parseExpression(equation.getInfix());
        
        return evaluationService.evaluate(expressionTree, variables);
    }
    
    
    private EquationDto convertToDto(EquationEntity entity) {
        return new EquationDto(
            entity.getId(),
            entity.getInfix(),
            entity.getPostfix()
        );
    }
    
   
    private EquationSummaryDto convertToSummaryDto(EquationEntity entity) {
        return new EquationSummaryDto(
            entity.getId(),
            entity.getInfix()
        );
    }
    

} 