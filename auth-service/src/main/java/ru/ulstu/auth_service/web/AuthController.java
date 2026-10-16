package ru.ulstu.auth_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.ulstu.auth_service.service.JwtService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Авторизация", description = "Выдача JWT-токенов")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, UserInfo> users = new HashMap<>();

    public AuthController(JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;

        users.put("admin", new UserInfo(
                passwordEncoder.encode("admin"),
                List.of("ADMIN", "USER")));
        users.put("teacher", new UserInfo(
                passwordEncoder.encode("teacher"),
                List.of("USER")));

        log.info("Auth users initialized: {}", users.keySet());
    }

    @PostMapping("/login")
    @Operation(summary = "Логин, выдаёт Bearer JWT")
    public LoginResponse login(@RequestBody LoginRequest request) {
        log.info("POST /api/auth/login user={}", request.username());

        UserInfo info = users.get(request.username());

        if (info == null || !passwordEncoder.matches(request.password(), info.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }

        String token = jwtService.generate(request.username(), info.roles());
        log.info("Login OK user={} roles={}", request.username(), info.roles());
        return new LoginResponse(token, "Bearer", 3600);
    }

    private record UserInfo(String passwordHash, List<String> roles) {}

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String accessToken, String tokenType, long expiresIn) {}
}