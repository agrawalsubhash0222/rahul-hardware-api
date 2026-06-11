package com.rahulhardware.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahulhardware.dto.OtpRequest;
import com.rahulhardware.dto.OtpVerifyRequest;
import com.rahulhardware.service.otp.OtpServiceWhatsapp;

@RestController
@RequestMapping("/auth/whatsapp")
public class AuthWhatsAppController {

    private final OtpServiceWhatsapp OtpServiceWhatsapp;

    public AuthWhatsAppController(OtpServiceWhatsapp OtpServiceWhatsapp) {
        this.OtpServiceWhatsapp = OtpServiceWhatsapp;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {
        try {
            OtpServiceWhatsapp.sendOtp(request.getMobile());

            return ResponseEntity.ok(
                    Map.of("message", "OTP sent successfully on WhatsApp"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "message", "Unable to send WhatsApp OTP",
                            "error", ex.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest request) {
        boolean verified = OtpServiceWhatsapp.verifyOtp(
                request.getMobile(),
                request.getOtp());

        if (!verified) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid or expired OTP"));
        }

        return ResponseEntity.ok(
                Map.of("message", "OTP verified successfully"));
    }
}