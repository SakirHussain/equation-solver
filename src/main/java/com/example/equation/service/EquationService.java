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

/**
 * Service layer for equation operations.
 * Provides high-level business logic for storing, retrieving, and evaluating equations.
 * Coordinates between parser, repository, and evaluation services.
 */
@Service
public class EquationService {
    
    private final EquationRepository equationRepository;
    private final ParserService parserService;
    private final EvaluationService evaluationService;
    
    /**
     * Constructor injection of dependencies.
     * 
     * @param equationRepository repository for equation persistence
     * @param parserService service for parsing mathematical expressions
     * @param evaluationService service for evaluating expression trees
     */
    public EquationService(EquationRepository equationRepository, 
                          ParserService parserService, 
                          EvaluationService evaluationService) {
        this.equationRepository = equationRepository;
        this.parserService = parserService;
        this.evaluationService = evaluationService;
    }
    
    /**
     * Stores a mathematical expression by parsing it and converting to postfix notation.
     * If the equation already exists, returns the existing equation's ID instead of creating a duplicate.
     * 
     * <p>Process:</p>
     * <ol>
     *   <li>Check if equation already exists</li>
     *   <li>If exists, return existing ID</li>
     *   <li>If not, tokenize the infix expression</li>
     *   <li>Convert to postfix notation using Shunting-Yard algorithm</li>
     *   <li>Validate syntax by building expression tree</li>
     *   <li>Extract token types for storage</li>
     *   <li>Create and save EquationEntity</li>
     * </ol>
     * 
     * @param infix the mathematical expression in infix notation
     * @return the unique ID of the stored equation (existing or newly created)
     * @throws IllegalArgumentException if infix is null or empty
     * @throws com.example.equation.exception.EquationSyntaxException if parsing fails
     */
    public Long storeEquation(String infix) {
        if (infix == null || infix.trim().isEmpty()) {
            throw new IllegalArgumentException("Infix expression cannot be null or empty");
        }
        
        String trimmedInfix = infix.trim();
        
        // Check if equation already exists
        Optional<EquationEntity> existingEquation = findEquationByInfix(trimmedInfix);
        if (existingEquation.isPresent()) {
            return existingEquation.get().getId();
        }
        
        // Parse and convert to postfix
        List<TokenValue> infixTokens = parserService.tokenize(trimmedInfix);
        List<TokenValue> postfixTokens = parserService.infixToPostfix(infixTokens);
        
        // Validate syntax by building the expression tree
        // This will throw EquationSyntaxException if the expression is invalid
        parserService.buildExpressionTree(postfixTokens);
        
        // Extract token types for storage (without values)
        List<Token> postfixTypes = postfixTokens.stream()
            .map(TokenValue::getType)
            .toList();
        
        // Create and save entity
        EquationEntity equation = new EquationEntity(null, trimmedInfix, postfixTypes);
        EquationEntity savedEquation = equationRepository.save(equation);
        
        return savedEquation.getId();
    }
    
    /**
     * Retrieves all stored equations as DTOs.
     * 
     * @return list of equation DTOs, empty list if no equations exist
     */
    public List<EquationDto> getAllEquations() {
        List<EquationEntity> entities = equationRepository.findAll();
        
        return entities.stream()
            .map(this::convertToDto)
            .toList();
    }
    
    /**
     * Retrieves all stored equations as simplified summary DTOs.
     * Contains only ID and infix expression for efficient listing.
     * 
     * @return list of equation summary DTOs, empty list if no equations exist
     */
    public List<EquationSummaryDto> getAllEquationSummaries() {
        List<EquationEntity> entities = equationRepository.findAll();
        
        return entities.stream()
            .map(this::convertToSummaryDto)
            .toList();
    }
    
    /**
     * Evaluates a stored equation with the provided variable values.
     * 
     * <p>Process:</p>
     * <ol>
     *   <li>Fetch equation by ID</li>
     *   <li>Parse the infix expression to build expression tree</li>
     *   <li>Evaluate the tree with provided variables</li>
     * </ol>
     * 
     * @param id the unique ID of the equation to evaluate
     * @param variables map of variable names to their values
     * @return the computed result of the equation
     * @throws IllegalArgumentException if id is null or variables map is null
     * @throws IllegalStateException if equation with given ID is not found
     * @throws com.example.equation.exception.VariableNotProvidedException if required variables are missing
     * @throws ArithmeticException for arithmetic errors like division by zero
     */
    public double evaluateEquation(Long id, Map<String, Double> variables) {
        if (id == null) {
            throw new IllegalArgumentException("Equation ID cannot be null");
        }
        if (variables == null) {
            throw new IllegalArgumentException("Variables map cannot be null");
        }
        
        // Fetch equation from repository
        Optional<EquationEntity> equationOpt = equationRepository.findById(id);
        if (equationOpt.isEmpty()) {
            throw new IllegalStateException("Equation with ID " + id + " not found");
        }
        
        EquationEntity equation = equationOpt.get();
        
        // Parse infix to build expression tree
        Node expressionTree = parserService.parseExpression(equation.getInfix());
        
        // Evaluate with provided variables
        return evaluationService.evaluate(expressionTree, variables);
    }
    
    /**
     * Converts an EquationEntity to an EquationDto.
     * 
     * @param entity the entity to convert
     * @return the corresponding DTO
     */
    private EquationDto convertToDto(EquationEntity entity) {
        return new EquationDto(
            entity.getId(),
            entity.getInfix(),
            entity.getPostfix()
        );
    }
    
    /**
     * Converts an EquationEntity to an EquationSummaryDto.
     * 
     * @param entity the entity to convert
     * @return the corresponding summary DTO
     */
    private EquationSummaryDto convertToSummaryDto(EquationEntity entity) {
        return new EquationSummaryDto(
            entity.getId(),
            entity.getInfix()
        );
    }
    
    /**
     * Finds an equation by its infix expression.
     * 
     * @param infix the infix expression to search for
     * @return Optional containing the equation if found, empty otherwise
     */
    private Optional<EquationEntity> findEquationByInfix(String infix) {
        List<EquationEntity> allEquations = equationRepository.findAll();
        return allEquations.stream()
            .filter(equation -> equation.getInfix().equals(infix))
            .findFirst();
    }
} 