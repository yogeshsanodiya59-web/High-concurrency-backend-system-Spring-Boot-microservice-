package com.grid07.assignment.dto;

import lombok.Data;

@Data
public class CreateCommentRequest {
    private Long authorId;


    private String authorType;


    private String content;

    private int depthLevel;
    private Long humanId;
}