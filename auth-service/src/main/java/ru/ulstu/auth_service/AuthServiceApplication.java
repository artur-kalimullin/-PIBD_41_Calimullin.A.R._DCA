package ru.ulstu.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {
    public static void main(String[] args) {
        reactor.core.publisher.Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}