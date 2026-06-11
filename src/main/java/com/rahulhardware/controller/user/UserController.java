package com.rahulhardware.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahulhardware.entity.User;
import com.rahulhardware.repository.UserRepository;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*") // 🔥 important for Expo
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // ✅ GET API
    @GetMapping
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    // ✅ POST API (THIS WAS MISSING)
    @PostMapping("/user")
    public User addUser(@RequestBody User user) {
        return userRepository.save(user);
    }
}