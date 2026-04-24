package com.grid07.assignment.dto;

import lombok.Data;

@Data
public class CreateUserRequest {

    private String username;


    private boolean premium;
}