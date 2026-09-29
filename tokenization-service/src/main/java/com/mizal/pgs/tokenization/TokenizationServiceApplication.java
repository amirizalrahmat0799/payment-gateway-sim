package com.mizal.pgs.tokenization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TokenizationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TokenizationServiceApplication.class, args);
    }
}
