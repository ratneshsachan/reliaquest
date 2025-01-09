package com.reliaquest.api.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private T data;
    private String status;
}
