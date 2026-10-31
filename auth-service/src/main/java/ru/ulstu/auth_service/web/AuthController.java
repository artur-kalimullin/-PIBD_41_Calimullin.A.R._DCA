package ru.ulstu.auth_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.ulstu.auth_service.service.JwtService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Авторизация", description = "Выдача JWT-токенов")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${course.discipline:Теория Автоматов}")
    private String discipline;

    private final Map<String, UserInfo> users = new HashMap<>();

    public AuthController(JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        users.put("admin", new UserInfo(passwordEncoder.encode("admin"),
                List.of("ADMIN", "USER"), "Калимуллин Артур"));
        users.put("teacher", new UserInfo(passwordEncoder.encode("teacher"),
                List.of("USER"), "Анатолий Борисович"));
        log.info("Auth users initialized: {}", users.keySet());
    }

    @PostMapping("/login")
    @Operation(summary = "Логин, выдаёт Bearer JWT")
    public Mono<LoginResponse> login(@RequestBody LoginRequest request) {
        return Mono.fromCallable(() -> {
            log.info("POST /api/auth/login user={}", request.username());

            UserInfo info = users.get(request.username());
            if (info == null || !passwordEncoder.matches(request.password(), info.passwordHash())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
            }

            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("fullName", info.fullName());
            if (!info.roles().contains("ADMIN")) {
                extra.put("discipline", discipline);
            }

            JwtService.TokenResult result = jwtService.generate(
                    request.username(), info.roles(), extra);

            log.info("Login OK user={} roles={}", request.username(), info.roles());
            return new LoginResponse(result.token(), "Bearer", 3600, result.payload());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private record UserInfo(String passwordHash, List<String> roles, String fullName) {}
    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String accessToken, String tokenType,
                                long expiresIn, Map<String, Object> payload) {}
}