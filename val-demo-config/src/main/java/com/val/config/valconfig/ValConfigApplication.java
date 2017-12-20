package com.val.config.valconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ValConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValConfigApplication.class, args);
    }
}
