package com.example.user_service.controller;

import com.example.user_service.model.User;
import com.example.user_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public User register(@RequestBody User user){
        return authService.register(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody User user){
        return authService.login(user);
    }
}
