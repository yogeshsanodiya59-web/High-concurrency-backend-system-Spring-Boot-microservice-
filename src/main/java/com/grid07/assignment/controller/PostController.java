package com.grid07.assignment.controller;

import com.grid07.assignment.dto.CreatePostRequest;
import com.grid07.assignment.entity.Post;
import com.grid07.assignment.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody CreatePostRequest request) {
        Post post = postService.createPost(

                request.getAuthorId(),

                request.getAuthorType(),
                request.getContent()
        );
        return ResponseEntity.ok(post);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> likePost(@PathVariable Long postId) {


        postService.likePost(postId);

        return ResponseEntity.ok("Post liked successfully");
    }
}