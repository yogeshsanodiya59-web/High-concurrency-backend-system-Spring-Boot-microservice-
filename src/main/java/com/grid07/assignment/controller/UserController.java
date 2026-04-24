package com.grid07.assignment.controller;

import com.grid07.assignment.dto.CreateUserRequest;
import com.grid07.assignment.entity.User;
import com.grid07.assignment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/users")

@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public User createUser(@RequestBody CreateUserRequest request) {
// Controller for communitaion via dto
          return userService.createUser(request.getUsername(), request.isPremium());
    }
}