package com.grid07.assignment.service;

import com.grid07.assignment.entity.User;
import com.grid07.assignment.repository.UserRepository;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(String username, boolean isPremium) {
        if (userRepository.existsByUsername(username)) {

            throw new RuntimeException("Username already exists: " + username);
        }
        User user = new User();
        user.setUsername(username);

        user.setPremium(isPremium);


        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)


                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}