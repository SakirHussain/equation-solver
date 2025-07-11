# Equation Service (Equation Solver API)

A Spring Boot REST API service for parsing, storing, and evaluating mathematical equations with variable substitution.

## Requirements  
- **Java 21** or higher  
- Maven (or use the included Maven wrapper)  
- 16MB+ RAM (for in-memory database)

## Setup  

1. **Clone the repository**  
   ```bash
   git clone https://github.com/SakirHussain/equation-solver.git
   cd equation-solver
   ```

2. **No additional database setup required**  
   - Uses `ConcurrentHashMap` (no persistence)
   - All data is stored in memory during runtime

## Building & Running  

### Quick Start  
```bash
./mvnw spring-boot:run
```
The application will start on `http://localhost:8080`.

### Build & Test  
```bash
# Compile
./mvnw clean compile

# Run tests
./mvnw test

# Package JAR
./mvnw package
```

## API Endpoints  

All endpoints are prefixed with `/api/equations`.

| Endpoint                                | Method | Description                                                     |
|-----------------------------------------|--------|-----------------------------------------------------------------|
| `/api/equations/store`                  | POST   | Store a new mathematical equation for future evaluation.        |
| `/api/equations`                        | GET    | Retrieve all stored equations with their IDs and expressions.  |
| `/api/equations/{id}/evaluate`          | POST   | Evaluate a stored equation by substituting variables.          |
| `/actuator/health`                      | GET    | Health check endpoint.                                          |

### Store Equation  
**Request**  
- URL: `/api/equations/store`  
- Method: POST
- Headers: `Content-Type: application/json`  
- Body:
  ```json
  {
    "equation": "x + y * 2"
  }
  ```
- Response:  
  ```json
  {
    "id": 1
  }
  ```

### Query All Equations 
**Request**  
- URL: `/api/equations`  
- Method: GET
    
- Response:
  ```json
  [
    {
      "id": 1,
      "infix": "x + y * 2"
    },
    {
      "id": 2,
      "infix": "a * b^2 + c"
    }
  ]
  ```

### Evaluate Equation
**Request**  
- URL: `/api/equations/{id}/evaluate`  
- Method: POST
- Headers: `Content-Type: application/json`  
- Body:
  ```json
  {
    "variables": {
      "x": 5.0,
      "y": 3.0
    }
  }
  ```
- Response:
  ```json
  {
    "result": 11.0
  }
  ```

## Supported Mathematical Operations

### Operators
- **Addition**: `+`
- **Subtraction**: `-`
- **Multiplication**: `*`
- **Division**: `/`
- **Exponentiation**: `^`

### Operator Precedence
1. Parentheses: `()`
2. Exponentiation: `^` (right-associative)
3. Multiplication and Division: `*`, `/` (left-associative)
4. Addition and Subtraction: `+`, `-` (left-associative)

### Examples
```
Expression: "x + y * 2"
Variables: {"x": 5, "y": 3}
Result: 11.0  // 5 + (3 * 2) = 5 + 6 = 11

Expression: "(a + b) * c"
Variables: {"a": 2, "b": 3, "c": 4}
Result: 20.0  // (2 + 3) * 4 = 5 * 4 = 20

Expression: "radius^2 * 3.14159"
Variables: {"radius": 5}
Result: 78.53975  // 25 * 3.14159 = 78.53975
```

## Advanced Features

### Duplicate Detection
- **AST-based deduplication**: The system uses Abstract Syntax Tree (AST) hashing to detect mathematically equivalent expressions
- **Whitespace independence**: `"x+y"` and `" x + y "` are treated as identical
- **Redundant parentheses**: `"a*b"` and `"(a*b)"` are recognized as equivalent

### Variable Support
- **Multi-character variables**: Support for variables like `x`, `y`, `radius`, `price`
- **Numeric constants**: Support for integers and decimals (e.g., `3.14159`, `42`)
- **Mixed expressions**: Combine variables and constants freely

### Error Handling
- **Syntax validation**: Detects invalid expressions like `"x + + y"` or `"3 * @ 2"`
- **Missing variables**: Clear error messages when variables are not provided
- **Division by zero**: Proper handling of arithmetic exceptions
- **Balanced parentheses**: Validates matching opening and closing parentheses

## Usage Examples

### Basic Arithmetic
```bash
# Store equation
curl -X POST http://localhost:8080/api/equations/store \
  -H "Content-Type: application/json" \
  -d '{"equation": "x + y * 2"}'

# Response: {"id": 1}

# Evaluate equation
curl -X POST http://localhost:8080/api/equations/1/evaluate \
  -H "Content-Type: application/json" \
  -d '{"variables": {"x": 10, "y": 5}}'

# Response: {"result": 20.0}
```

### Complex Expressions
```bash
# Quadratic formula: ax² + bx + c
curl -X POST http://localhost:8080/api/equations/store \
  -H "Content-Type: application/json" \
  -d '{"equation": "a*x^2 + b*x + c"}'

# Evaluate with a=1, b=2, c=3, x=4
curl -X POST http://localhost:8080/api/equations/1/evaluate \
  -H "Content-Type: application/json" \
  -d '{"variables": {"a": 1, "b": 2, "c": 3, "x": 4}}'

# Response: {"result": 27.0}  // 1*16 + 2*4 + 3 = 16 + 8 + 3 = 27
```

### Financial Calculations
```bash
# Compound interest: P * (1 + r/n)^(n*t)
curl -X POST http://localhost:8080/api/equations/store \
  -H "Content-Type: application/json" \
  -d '{"equation": "P * (1 + r/n)^(n*t)"}'

# Calculate $1000 at 5% annual rate, quarterly compounding, 3 years
curl -X POST http://localhost:8080/api/equations/1/evaluate \
  -H "Content-Type: application/json" \
  -d '{"variables": {"P": 1000, "r": 0.05, "n": 4, "t": 3}}'

# Response: {"result": 1160.755}
```

## Tech Stack  
- **Java 21** - Modern Java features and performance
- **Spring Boot 3.5.3** - Framework for REST API development
- **Spring Web** - REST controller support
- **Spring Validation** - Request validation
- **Spring Actuator** - Health monitoring and metrics

## Project Structure  
```
equation-solver/
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── equation/
│   │   │   │   ├── controller/       # REST controllers
│   │   │   │   ├── dto/              # Request/response DTOs
│   │   │   │   ├── exception/        # Custom exceptions
│   │   │   │   ├── model/            # Data models and AST nodes
│   │   │   │   ├── repository/       # Data access layer
│   │   │   │   └── service/          # Business logic
│   │   │   └── equationservice/
│   │   │       └── EquationServiceApplication.java
│   │   └── resources/
│   │       └── application.yml       # Application configuration
│   └── test/
│       └── java/                    
├── mvnw, mvnw.cmd                    
├── pom.xml                           
└── README.md                         
```
