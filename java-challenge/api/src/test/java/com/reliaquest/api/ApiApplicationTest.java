package com.reliaquest.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.reliaquest.api.controller.EmployeeController;
import com.reliaquest.api.dto.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeRequest;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class ApiApplicationTest {

    @Autowired
    private EmployeeController employeeController;

    @MockBean
    private RestTemplate restTemplate;

    @Value("${mock.api.base-url}")
    private String baseUrl;

    @Test
    @DisplayName("Should retrieve all employees successfully")
    void getAllEmployees_Success() {
        // Create test data
        Employee emp1 = new Employee();
        emp1.setId("1");
        emp1.setEmployee_name("John Doe");
        emp1.setEmployee_salary(50000);
        emp1.setEmployee_age(30);
        emp1.setEmployee_title("Developer");

        Employee emp2 = new Employee();
        emp2.setId("2");
        emp2.setEmployee_name("Jane Smith");
        emp2.setEmployee_salary(60000);
        emp2.setEmployee_age(35);
        emp2.setEmployee_title("Senior Developer");

        List<Employee> employees = Arrays.asList(emp1, emp2);

        // Setup mock response
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(employees);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Execute and verify
        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getEmployee_name()).isEqualTo("John Doe");
        assertThat(response.getBody().get(1).getEmployee_name()).isEqualTo("Jane Smith");
    }

    @Test
    @DisplayName("Should find employees by name search")
    void getEmployeesByNameSearch_Success() {
        // Create test data
        Employee emp1 = new Employee();
        emp1.setId("1");
        emp1.setEmployee_name("John Doe");
        emp1.setEmployee_salary(50000);

        Employee emp2 = new Employee();
        emp2.setId("2");
        emp2.setEmployee_name("Johnny Smith");
        emp2.setEmployee_salary(60000);

        List<Employee> employees = Arrays.asList(emp1, emp2);

        // Setup mock response
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(employees);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Execute and verify
        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch("John");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should retrieve employee by ID successfully")
    void getEmployeeById_Success() {
        // Create test employee
        Employee employee = new Employee();
        employee.setId("1");
        employee.setEmployee_name("John Doe");
        employee.setEmployee_salary(50000);
        employee.setEmployee_age(30);
        employee.setEmployee_title("Developer");

        // Setup mock response
        ApiResponse<Employee> apiResponse = new ApiResponse<>();
        apiResponse.setData(employee);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Execute and verify
        ResponseEntity<Employee> response = employeeController.getEmployeeById("1");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo("1");
        assertThat(response.getBody().getEmployee_name()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return the highest salary among employees")
    void getHighestSalary_Success() {
        // Create test data
        Employee emp1 = new Employee();
        emp1.setEmployee_salary(50000);

        Employee emp2 = new Employee();
        emp2.setEmployee_salary(75000);

        List<Employee> employees = Arrays.asList(emp1, emp2);

        // Setup mock response
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(employees);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Execute and verify
        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(75000);
    }

    @Test
    @DisplayName("Should return top 10 highest earning employees")
    void getTopTenHighestEarningEmployeeNames_Success() {
        // Create test data
        Employee emp1 = new Employee();
        emp1.setEmployee_name("John Doe");
        emp1.setEmployee_salary(100000);

        Employee emp2 = new Employee();
        emp2.setEmployee_name("Jane Smith");
        emp2.setEmployee_salary(90000);

        Employee emp3 = new Employee();
        emp3.setEmployee_name("Bob Johnson");
        emp3.setEmployee_salary(80000);

        List<Employee> employees = Arrays.asList(emp1, emp2, emp3);

        // Setup mock response
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>();
        apiResponse.setData(employees);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Execute and verify
        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody().get(0)).isEqualTo("John Doe");
        assertThat(response.getBody().get(1)).isEqualTo("Jane Smith");
        assertThat(response.getBody().get(2)).isEqualTo("Bob Johnson");
    }

    @Test
    @DisplayName("Should create new employee successfully")
    void createEmployee_Success() {
        // Create request
        EmployeeRequest request = new EmployeeRequest();
        request.setName("John Doe");
        request.setSalary(50000);
        request.setAge(30);
        request.setTitle("Developer");

        // Create expected response
        Employee createdEmployee = new Employee();
        createdEmployee.setId("1");
        createdEmployee.setEmployee_name("John Doe");
        createdEmployee.setEmployee_salary(50000);
        createdEmployee.setEmployee_age(30);
        createdEmployee.setEmployee_title("Developer");

        // Setup mock response
        ApiResponse<Employee> apiResponse = new ApiResponse<>();
        apiResponse.setData(createdEmployee);

        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Execute and verify
        ResponseEntity<Employee> response = employeeController.createEmployee(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmployee_name()).isEqualTo("John Doe");
        assertThat(response.getBody().getEmployee_salary()).isEqualTo(50000);
    }

    @Test
    @DisplayName("Should delete employee successfully")
    void deleteEmployeeById_Success() {
        // Create employee to be deleted
        Employee employee = new Employee();
        employee.setId("1");
        employee.setEmployee_name("John Doe");

        // Setup mock response for getEmployeeById
        ApiResponse<Employee> apiResponse = new ApiResponse<>();
        apiResponse.setData(employee);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        // Setup mock for delete
        doNothing().when(restTemplate).delete(anyString(), any(ResponseEntity.class));

        // Execute and verify
        ResponseEntity<String> response = employeeController.deleteEmployeeById("1");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("John Doe");
    }
}
