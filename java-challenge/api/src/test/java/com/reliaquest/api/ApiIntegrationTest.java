package com.reliaquest.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.reliaquest.api.controller.EmployeeController;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class ApiIntegrationTest {

    @Autowired
    private EmployeeController employeeController;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${mock.api.base-url}")
    private String baseUrl;

    @Test
    @DisplayName("Should create new employee with provided details")
    void createEmployee_Integration() {
        // Create new employee
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Jill Jenkins");
        request.setId(UUID.randomUUID().toString());
        request.setSalary(139084);
        request.setAge(48);
        request.setTitle("Financial Advisor");

        ResponseEntity<Employee> response = employeeController.createEmployee(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmployee_name()).isEqualTo("Jill Jenkins");
        assertThat(response.getBody().getEmployee_salary()).isEqualTo(139084);
        assertThat(response.getBody().getEmployee_age()).isEqualTo(48);
        assertThat(response.getBody().getEmployee_title()).isEqualTo("Financial Advisor");

        // Cleanup
        if (response.getBody() != null) {
            employeeController.deleteEmployeeById(response.getBody().getId());
        }
    }

    @Test
    @DisplayName("Should perform complete employee lifecycle operations")
    void employeeLifecycle_Integration() {
        // Create employee
        EmployeeRequest request = new EmployeeRequest();
        request.setName("Test Employee");
        request.setSalary(50000);
        request.setAge(30);
        request.setTitle("Test Engineer");

        ResponseEntity<Employee> createResponse = employeeController.createEmployee(request);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        String employeeId = createResponse.getBody().getId();

        // Get by ID
        ResponseEntity<Employee> getResponse = employeeController.getEmployeeById(employeeId);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getEmployee_name()).isEqualTo("Test Employee");

        // Get all and verify
        ResponseEntity<List<Employee>> allResponse = employeeController.getAllEmployees();
        assertThat(allResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(allResponse.getBody()).isNotNull();
        assertThat(allResponse.getBody()).isNotEmpty();
        assertThat(allResponse.getBody().stream().anyMatch(emp -> emp.getId().equals(employeeId)))
                .isTrue();

        // Search by name
        ResponseEntity<List<Employee>> searchResponse = employeeController.getEmployeesByNameSearch("Test");
        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResponse.getBody()).isNotNull();
        assertThat(searchResponse.getBody()).isNotEmpty();
        assertThat(searchResponse.getBody().stream()
                        .anyMatch(emp -> emp.getEmployee_name().contains("Test")))
                .isTrue();

        // Delete
        ResponseEntity<String> deleteResponse = employeeController.deleteEmployeeById(employeeId);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify deletion
        ResponseEntity<Employee> verifyDelete = employeeController.getEmployeeById(employeeId);
        assertThat(verifyDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should perform salary-based operations across employees")
    void salaryOperations_Integration() {
        // Create multiple employees with different salaries
        EmployeeRequest request1 = new EmployeeRequest();
        request1.setName("High Salary Employee");
        request1.setSalary(1500000);
        request1.setAge(35);
        request1.setTitle("Senior Engineer");

        EmployeeRequest request2 = new EmployeeRequest();
        request2.setName("Mid Salary Employee");
        request2.setSalary(1000000);
        request2.setAge(30);
        request2.setTitle("Engineer");

        EmployeeRequest request3 = new EmployeeRequest();
        request3.setName("Entry Salary Employee");
        request3.setSalary(750000);
        request3.setAge(25);
        request3.setTitle("Junior Engineer");

        ResponseEntity<Employee> response1 = employeeController.createEmployee(request1);
        ResponseEntity<Employee> response2 = employeeController.createEmployee(request2);
        ResponseEntity<Employee> response3 = employeeController.createEmployee(request3);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Get highest salary
        ResponseEntity<Integer> highestSalary = employeeController.getHighestSalaryOfEmployees();
        assertThat(highestSalary.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(highestSalary.getBody()).isNotNull();
        assertThat(highestSalary.getBody()).isGreaterThanOrEqualTo(1500000);

        // Get top earners
        ResponseEntity<List<String>> topEarners = employeeController.getTopTenHighestEarningEmployeeNames();
        assertThat(topEarners.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(topEarners.getBody()).isNotNull();
        assertThat(topEarners.getBody())
                .contains("High Salary Employee")
                .contains("Mid Salary Employee")
                .contains("Entry Salary Employee");

        // Cleanup
        if (response1.getBody() != null) {
            employeeController.deleteEmployeeById(response1.getBody().getId());
        }
        if (response2.getBody() != null) {
            employeeController.deleteEmployeeById(response2.getBody().getId());
        }
        if (response3.getBody() != null) {
            employeeController.deleteEmployeeById(response3.getBody().getId());
        }
    }

    @Test
    @DisplayName("Should perform various search operations on employees")
    void searchOperations_Integration() {
        // Create employees with similar names
        EmployeeRequest request1 = new EmployeeRequest();
        request1.setName("John Developer");
        request1.setSalary(80000);
        request1.setAge(28);
        request1.setTitle("Developer");

        EmployeeRequest request2 = new EmployeeRequest();
        request2.setName("Johnny Engineer");
        request2.setSalary(85000);
        request2.setAge(30);
        request2.setTitle("Engineer");

        ResponseEntity<Employee> response1 = employeeController.createEmployee(request1);
        ResponseEntity<Employee> response2 = employeeController.createEmployee(request2);

        // Search with exact name
        ResponseEntity<List<Employee>> exactSearch = employeeController.getEmployeesByNameSearch("John Developer");
        assertThat(exactSearch.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exactSearch.getBody()).isNotNull();
        assertThat(exactSearch.getBody().stream()
                        .anyMatch(emp -> emp.getEmployee_name().equals("John Developer")))
                .isTrue();

        // Search with partial name
        ResponseEntity<List<Employee>> partialSearch = employeeController.getEmployeesByNameSearch("John");
        assertThat(partialSearch.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(partialSearch.getBody()).isNotNull();
        assertThat(partialSearch.getBody().size()).isGreaterThanOrEqualTo(2);

        // Search with case-insensitive name
        ResponseEntity<List<Employee>> caseInsensitiveSearch = employeeController.getEmployeesByNameSearch("john");
        assertThat(caseInsensitiveSearch.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(caseInsensitiveSearch.getBody()).isNotNull();
        assertThat(caseInsensitiveSearch.getBody().size()).isGreaterThanOrEqualTo(2);

        // Cleanup
        if (response1.getBody() != null) {
            employeeController.deleteEmployeeById(response1.getBody().getId());
        }
        if (response2.getBody() != null) {
            employeeController.deleteEmployeeById(response2.getBody().getId());
        }
    }

    @Test
    @DisplayName("Should update existing employee information")
    void employeeUpdate_Integration() {
        // Create initial employee
        EmployeeRequest createRequest = new EmployeeRequest();
        createRequest.setName("Initial Name");
        createRequest.setSalary(60000);
        createRequest.setAge(25);
        createRequest.setTitle("Initial Title");

        ResponseEntity<Employee> createResponse = employeeController.createEmployee(createRequest);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();

        String employeeId = createResponse.getBody().getId();

        // Create update request
        EmployeeRequest updateRequest = new EmployeeRequest();
        updateRequest.setId(employeeId);
        updateRequest.setName("Updated Name");
        updateRequest.setSalary(65000);
        updateRequest.setAge(26);
        updateRequest.setTitle("Updated Title");

        ResponseEntity<Employee> updateResponse = employeeController.createEmployee(updateRequest);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getEmployee_name()).isEqualTo("Updated Name");
        assertThat(updateResponse.getBody().getEmployee_salary()).isEqualTo(65000);

        // Cleanup
        employeeController.deleteEmployeeById(employeeId);
    }

    @Test
    @DisplayName("Should handle invalid operations appropriately")
    void invalidOperations_Integration() {
        // Try to get non-existent employee
        String nonExistentId = UUID.randomUUID().toString();
        ResponseEntity<Employee> getResponse = employeeController.getEmployeeById(nonExistentId);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Try to delete non-existent employee
        ResponseEntity<String> deleteResponse = employeeController.deleteEmployeeById(nonExistentId);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
