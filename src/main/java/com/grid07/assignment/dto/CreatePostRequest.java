package com.grid07.assignment.dto;

import lombok.Data;

@Data
public class CreatePostRequest {
    private Long authorId;


    private String authorType;

    private String content;
}