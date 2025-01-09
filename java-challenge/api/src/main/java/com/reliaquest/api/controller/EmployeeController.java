package com.reliaquest.api.controller;

import com.reliaquest.api.dto.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeRequest;
import jakarta.validation.Valid;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController implements IEmployeeController<Employee, EmployeeRequest> {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String API_PATH = "/api/v1/employee";
    private static final int MAX_CONCURRENT_REQUESTS = 100;
    private static final int REQUEST_TIMEOUT = 5000; // 5 seconds

    private final Semaphore requestLimiter = new Semaphore(MAX_CONCURRENT_REQUESTS, true);
    private final AtomicInteger activeRequests = new AtomicInteger(0);

    public EmployeeController(RestTemplate restTemplate, @Value("${mock.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Fetching all employees. Active requests: {}", activeRequests.get());

        if (!acquireRequestPermit()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        try {

            String url = baseUrl + API_PATH;
            ResponseEntity<ApiResponse<List<Employee>>> response =
                    executeGetRequest(url, new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {});

            ResponseEntity<List<Employee>> result = handleListResponse(response, "Error fetching all employees");

            return result;
        } catch (Exception e) {
            log.error("Error fetching all employees", e);
            return handleException(e);
        } finally {
            releaseRequestPermit();
        }
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        log.info("Searching employees with name containing: {}", searchString);

        if (!acquireRequestPermit()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        try {

            return executeWithTimeout(() -> {
                ResponseEntity<List<Employee>> allEmployeesResponse = getAllEmployees();
                if (!isValidResponse(allEmployeesResponse)) {
                    return allEmployeesResponse;
                }

                List<Employee> matchedEmployees = filterEmployeesByName(allEmployeesResponse.getBody(), searchString);

                return ResponseEntity.ok(matchedEmployees);
            });
        } catch (Exception e) {
            log.error("Error searching employees by name: {}", searchString, e);
            return handleException(e);
        } finally {
            releaseRequestPermit();
        }
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ResponseEntity<Employee> getEmployeeById(String id) {
        log.info("Fetching employee with id: {}", id);

        if (!acquireRequestPermit()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        try {

            String url = baseUrl + API_PATH + "/" + id;
            ResponseEntity<ApiResponse<Employee>> response =
                    executeGetRequest(url, new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            ResponseEntity<Employee> result = handleSingleResponse(response, "Employee not found with id: " + id);

            return result;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Employee not found with id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching employee with id: {}", id, e);
            return handleException(e);
        } finally {
            releaseRequestPermit();
        }
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Fetching highest salary");

        if (!acquireRequestPermit()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        try {

            ResponseEntity<List<Employee>> employeesResponse = getAllEmployees();
            if (!isValidResponse(employeesResponse)) {
                return ResponseEntity.status(employeesResponse.getStatusCode()).build();
            }

            Integer highestSalary = calculateHighestSalary(employeesResponse.getBody());
            return ResponseEntity.ok(highestSalary);
        } catch (Exception e) {
            log.error("Error calculating highest salary", e);
            return handleException(e);
        } finally {
            releaseRequestPermit();
        }
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Fetching top 10 highest earning employee names");

        if (!acquireRequestPermit()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        try {

            ResponseEntity<List<Employee>> employeesResponse = getAllEmployees();
            if (!isValidResponse(employeesResponse)) {
                return ResponseEntity.status(employeesResponse.getStatusCode()).build();
            }

            List<String> topEarners = getTopEarners(employeesResponse.getBody(), DEFAULT_PAGE_SIZE);
            return ResponseEntity.ok(topEarners);
        } catch (Exception e) {
            log.error("Error fetching top earners", e);
            return handleException(e);
        } finally {
            releaseRequestPermit();
        }
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeRequest employeeInput) {
        log.info("Creating new employee: {}", employeeInput.getName());

        if (!acquireRequestPermit()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        try {
            HttpEntity<EmployeeRequest> requestEntity = createRequestEntity(employeeInput);
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    baseUrl + API_PATH,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            ResponseEntity<Employee> result = handleSingleResponse(response, "Error creating employee");
            if (result.getStatusCode() == HttpStatus.OK) {}
            return result;
        } catch (Exception e) {
            log.error("Error creating employee: {}", employeeInput.getName(), e);
            return handleException(e);
        } finally {
            releaseRequestPermit();
        }
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ResponseEntity<String> deleteEmployeeById(String id) {
        log.info("Deleting employee with id: {}", id);

        if (!acquireRequestPermit()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        try {
            ResponseEntity<Employee> employeeResponse = getEmployeeById(id);
            if (!isValidResponse(employeeResponse)) {
                return ResponseEntity.status(employeeResponse.getStatusCode()).build();
            }

            String employeeName = employeeResponse.getBody().getEmployee_name();
            executeDeleteRequest(employeeName);
            return ResponseEntity.ok(employeeName);
        } catch (Exception e) {
            log.error("Error deleting employee with id: {}", id, e);
            return handleException(e);
        } finally {
            releaseRequestPermit();
        }
    }

    // Helper methods
    private boolean acquireRequestPermit() {
        try {
            return requestLimiter.tryAcquire(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void releaseRequestPermit() {
        requestLimiter.release();
        activeRequests.decrementAndGet();
    }

    private <T> ResponseEntity<ApiResponse<T>> executeGetRequest(
            String url, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        return restTemplate.exchange(url, HttpMethod.GET, null, responseType);
    }

    private void executeDeleteRequest(String employeeName) {
        HttpEntity<Map<String, String>> requestEntity = createDeleteRequestEntity(employeeName);
        restTemplate.exchange(
                baseUrl + API_PATH,
                HttpMethod.DELETE,
                requestEntity,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {});
    }

    private <T> ResponseEntity<T> executeWithTimeout(Callable<ResponseEntity<T>> operation) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<ResponseEntity<T>> future = executor.submit(operation);
            return future.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
        } catch (Exception e) {
            return handleException(e);
        } finally {
            executor.shutdown();
        }
    }

    private <T> HttpEntity<T> createRequestEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Map<String, String>> createDeleteRequestEntity(String employeeName) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", employeeName);
        return createRequestEntity(requestBody);
    }

    private <T> boolean isValidResponse(ResponseEntity<T> response) {
        return response.getStatusCode().is2xxSuccessful() && response.getBody() != null;
    }

    private List<Employee> filterEmployeesByName(List<Employee> employees, String searchString) {
        return employees.stream()
                .filter(emp -> emp.getEmployee_name().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
    }

    private Integer calculateHighestSalary(List<Employee> employees) {
        return employees.stream().mapToInt(Employee::getEmployee_salary).max().orElse(0);
    }

    private List<String> getTopEarners(List<Employee> employees, int limit) {
        return employees.stream()
                .sorted((e1, e2) -> e2.getEmployee_salary().compareTo(e1.getEmployee_salary()))
                .limit(limit)
                .map(Employee::getEmployee_name)
                .collect(Collectors.toList());
    }

    private <T> ResponseEntity<List<T>> handleListResponse(
            ResponseEntity<ApiResponse<List<T>>> response, String errorMessage) {
        if (response.getBody() != null && response.getBody().getData() != null) {
            return ResponseEntity.ok(response.getBody().getData());
        }
        log.warn(errorMessage);
        return ResponseEntity.ok(Collections.emptyList());
    }

    private <T> ResponseEntity<T> handleSingleResponse(ResponseEntity<ApiResponse<T>> response, String errorMessage) {
        if (response.getBody() != null && response.getBody().getData() != null) {
            return ResponseEntity.ok(response.getBody().getData());
        }
        log.warn(errorMessage);
        return ResponseEntity.notFound().build();
    }

    private <T> ResponseEntity<T> handleException(Exception e) {
        if (e instanceof HttpClientErrorException.NotFound) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
