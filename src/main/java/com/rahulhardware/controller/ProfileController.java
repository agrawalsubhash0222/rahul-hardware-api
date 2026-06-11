package com.rahulhardware.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahulhardware.dto.UpdateProfileRequest;
import com.rahulhardware.entity.User;
import com.rahulhardware.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{mobile}")
    public ResponseEntity<?> getProfile(@PathVariable String mobile) {

        User user = findUserByMobile(mobile);

        return ResponseEntity.ok(user);
    }

    @PutMapping("/{mobile}")
    public ResponseEntity<?> updateProfile(
            @PathVariable String mobile,
            @Valid @RequestBody UpdateProfileRequest request) {

        User user = findUserByMobile(mobile);

        String firstName = request.getFirstName() == null ? "" : request.getFirstName().trim();
        String lastName = request.getLastName() == null ? "" : request.getLastName().trim();
        String email = request.getEmail() == null ? "" : request.getEmail().trim();

        if (firstName.isEmpty()) {
            return ResponseEntity.badRequest().body("First name is required");
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setProfileImageUrl(request.getProfileImageUrl());

        String fullName = lastName.isEmpty() ? firstName : firstName + " " + lastName;
        user.setName(fullName);

        if (request.getLastName() != null
                && !request.getLastName().trim().isEmpty()) {

            fullName = request.getFirstName()
                    + " "
                    + request.getLastName();
        }

        user.setName(fullName);

        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(savedUser);
    }

    private User findUserByMobile(String mobile) {

        String cleanMobile = mobile == null
                ? ""
                : mobile.trim();

        if (cleanMobile.isEmpty()) {
            throw new RuntimeException("Mobile number is required");
        }

        final String mobileWithPlus91 = cleanMobile.startsWith("+91")
                ? cleanMobile
                : "+91" + cleanMobile;

        final String mobileWithoutPlus = cleanMobile.startsWith("+")
                ? cleanMobile.substring(1)
                : cleanMobile;

        final String mobileOnlyTenDigit;

        if (cleanMobile.startsWith("+91")) {

            mobileOnlyTenDigit = cleanMobile.substring(3);

        } else if (cleanMobile.startsWith("91")
                && cleanMobile.length() == 12) {

            mobileOnlyTenDigit = cleanMobile.substring(2);

        } else {

            mobileOnlyTenDigit = cleanMobile;
        }

        return userRepository.findByMobile(cleanMobile)

                .or(() -> userRepository.findByMobile(
                        mobileWithPlus91))

                .or(() -> userRepository.findByMobile(
                        mobileWithoutPlus))

                .or(() -> userRepository.findByMobile(
                        mobileOnlyTenDigit))

                .orElseThrow(() -> new RuntimeException(
                        "User not found for mobile: "
                                + mobile));
    }
}