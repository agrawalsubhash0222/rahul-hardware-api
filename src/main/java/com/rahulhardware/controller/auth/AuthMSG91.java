package com.rahulhardware.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rahulhardware.service.otp.OtpMSG91;

@CrossOrigin(origins = "http://localhost:8082")
@RestController
@RequestMapping("/auth")
public class AuthMSG91 {

    @Autowired
    private OtpMSG91 otpMSG91;

    @PostMapping("/send-otp-msg91")
    public ResponseEntity<?> sendOtpMSG91(@RequestParam String phone) {
        return ResponseEntity.ok(otpMSG91.sendOtp(phone));
    }

    @PostMapping("/verify-otp-msg91")
    public ResponseEntity<?> verifyOtpMSG91(@RequestParam String phone,
            @RequestParam String otp) {

        String response = otpMSG91.verifyOtp(phone, otp);

        if (response.contains("success")) {
            // ✅ Save user in DB here
            return ResponseEntity.ok("OTP Verified");
        } else {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }
    }
}