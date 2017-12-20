package com.val.service.valservice;

import com.val.service.valservice.configuration.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class ValServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValServiceApplication.class, args);
    }
}
