package com.reliaquest.api.model;

import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.Data;

@Data
public class EmployeeRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String id = UUID.randomUUID().toString(); // Automatically generates a UUID.

    @Positive(message = "Salary must be greater than zero") private Integer salary;

    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age must not exceed 75")
    private Integer age;

    @NotBlank(message = "Title is required")
    private String title;

    private String email;
}
