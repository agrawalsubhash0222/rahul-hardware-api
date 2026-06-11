package com.rahulhardware.service.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;

@Service
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.from.number}")
    private String fromNumber;

    @PostConstruct
    public void initTwilio() {

        Twilio.init(accountSid, authToken);
    }

    public void sendOrderSuccessSms(
            String customerMobile,
            String customerName,
            Double totalAmount
    ) {

        String messageBody =
                "Hi " + customerName +
                ", your order has been placed successfully. " +
                "Total amount: ₹" + totalAmount +
                ". Thank you for shopping with Rahul Hardware.";

        Message.creator(
                new PhoneNumber("+91" + customerMobile),
                new PhoneNumber(fromNumber),
                messageBody
        ).create();
    }
}