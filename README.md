## Project Overview

This is a Spring Boot-based Employee Management API that provides comprehensive functionality for managing employee records through a RESTful interface. The application integrates with a Mock Employee API and implements robust error handling, logging, and request management.

## Features

- Retrieve all employees
- Search employees by name
- Get employee by ID
- Find highest salary
- Get top earning employees
- Create new employees
- Delete employees
- Advanced request handling with:
  - Retry mechanisms
  - Concurrent request limiting
  - Timeout management

## Technical Specifications

### Core Technologies
- Java 17+
- Spring Boot
- Gradle
- Spring Web
- RestTemplate
- Lombok
- SLF4J Logging

### Key Architecture Components
- Concurrent request management
- Retry mechanism with exponential backoff
- Semaphore-based request limiting
- Comprehensive error handling
- Logging for all critical operations

## Endpoints

### 1. Get All Employees
- **Endpoint:** `/api/v1/employee`
- **Method:** GET
- **Description:** Retrieves all employees from the system

### 2. Search Employees by Name
- **Endpoint:** `/api/v1/employee/search?name={searchString}`
- **Method:** GET
- **Description:** Finds employees whose names contain the given search string

### 3. Get Employee by ID
- **Endpoint:** `/api/v1/employee/{id}`
- **Method:** GET
- **Description:** Retrieves a specific employee by their unique identifier

### 4. Get Highest Salary
- **Endpoint:** `/api/v1/employee/highest-salary`
- **Method:** GET
- **Description:** Returns the highest salary among all employees

### 5. Get Top 10 Highest Earning Employees
- **Endpoint:** `/api/v1/employee/top-earners`
- **Method:** GET
- **Description:** Retrieves names of top 10 highest-paid employees

### 6. Create Employee
- **Endpoint:** `/api/v1/employee`
- **Method:** POST
- **Description:** Creates a new employee record
- **Request Body:** Employee details (name, salary, age, title)

### 7. Delete Employee
- **Endpoint:** `/api/v1/employee/{id}`
- **Method:** DELETE
- **Description:** Deletes an employee by their unique identifier

## Request Handling Features

### Concurrency Management
- Maximum 100 concurrent requests
- 5-second timeout for request acquisition
- Graceful handling of request overflow

### Error Handling
- Retry mechanism (3 attempts with 1-second delay)
- Comprehensive logging
- Proper HTTP status code management

## Configuration

### Required Properties
```properties
mock.api.base-url=http://localhost:8112
```

### Dependency Requirements
- Spring Boot Starter Web
- Spring Retry
- Lombok
- SLF4J

## Running the Application

### Mock Employee API (Server Module)
Start the Server Spring Boot application:
```bash
./gradlew server:bootRun
```

**Note:** 
- Each invocation of the Server application triggers a new list of mock employee data
- Keep the server running for consistent data during testing
- The web server will randomly choose when to rate limit requests

### Main Application
```bash
./gradlew api:bootRun
```

## Code Formatting

This project uses [Diffplug Spotless](https://github.com/diffplug/spotless/tree/main/plugin-gradle) to enforce formatting and style guidelines.

To resolve any formatting errors:
```bash
./gradlew spotlessApply
```

## Build and Test

### Build the Project
```bash
./gradlew clean build
```

### Run Tests
```bash
./gradlew test
```

## Testing Strategy

### Test Coverage
- Integration Tests
  - Complete employee lifecycle
  - Concurrency scenarios
  - Error handling
- Unit Tests
  - Controller methods
  - Request processing
  - Error scenarios

### Test Frameworks
- JUnit 5
- Spring Boot Test
- Mockito
- AssertJ

## Performance Considerations

- Request limiter prevents system overload
- Timeout mechanisms protect against hanging requests
- Efficient stream processing for data manipulation
- Logarithmic search and filter operations

## Logging

Comprehensive logging implemented using SLF4J:
- Information logs for all API interactions
- Warning logs for potential issues
- Error logs with stack traces
- Request tracking and performance monitoring

## Security Considerations

- Input validation
- Controlled concurrent access
- Error information masking
- Retry mechanism prevents temporary failure escalation

## Potential Improvements
- Implement more granular error handling
- Add authentication and authorization
- Implement caching mechanisms
- Enhanced monitoring and metrics

## Contribution Guidelines
1. Follow existing code style
2. Write comprehensive tests
3. Update documentation
4. Ensure no breaking changes
5. Run `./gradlew spotlessApply` before committing

## License
Proprietary - Internal Use Only

---

**Note:** Ensure Mock Employee API is running at `http://localhost:8112` before starting the application.
