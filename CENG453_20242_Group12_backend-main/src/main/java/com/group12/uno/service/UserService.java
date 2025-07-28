package com.group12.uno.service;

import com.group12.uno.dto.UserResponse;
import com.group12.uno.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                        .username(user.getUsername())
                        .build())
                .collect(Collectors.toList());
    }
} 