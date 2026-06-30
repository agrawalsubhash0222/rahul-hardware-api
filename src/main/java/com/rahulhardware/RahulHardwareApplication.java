package com.rahulhardware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RahulHardwareApplication {
    public static void main(String[] a) {
        SpringApplication.run(RahulHardwareApplication.class, a);
    }
}
