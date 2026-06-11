package com.rahulhardware.service.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

import jakarta.annotation.PostConstruct;

@Service
public class OrderSmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.from.number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }

    public void sendOrderPlacedMessage(String customerMobile, String orderId, double amount) {

        String messageBody = "Dear Customer, your order has been placed successfully. "
                + "Order ID: " + orderId
                + ", Amount: Rs." + amount
                + ". Thank you for shopping with Rahul Hardware.";

        Message.creator(
                new com.twilio.type.PhoneNumber("+91" + customerMobile),
                new com.twilio.type.PhoneNumber(fromPhoneNumber),
                messageBody
        ).create();
    }
}