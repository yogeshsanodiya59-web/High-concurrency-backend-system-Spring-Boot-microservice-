package com.grid07.assignment.controller;

import com.grid07.assignment.dto.CreateCommentRequest;
import com.grid07.assignment.entity.Comment;
import com.grid07.assignment.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {

    private final PostService postService;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long postId,
            @RequestBody CreateCommentRequest request
    ) {
//        Creatingg the post service
        Comment comment = postService.addComment(
                postId,
                request.getAuthorId(),

                request.getAuthorType(),


                request.getContent(),
                request.getDepthLevel(),


                request.getHumanId()
        );
        return ResponseEntity.ok(comment);
    }
}