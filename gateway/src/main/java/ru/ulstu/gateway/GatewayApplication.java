package ru.ulstu.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        reactor.core.publisher.Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(GatewayApplication.class, args);
    }
}