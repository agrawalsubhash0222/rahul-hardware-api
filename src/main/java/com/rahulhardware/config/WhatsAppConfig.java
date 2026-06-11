package com.rahulhardware.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WhatsAppConfig {

    @Bean
    public RestClient whatsappRestClient() {
        return RestClient.builder().build();
    }
}