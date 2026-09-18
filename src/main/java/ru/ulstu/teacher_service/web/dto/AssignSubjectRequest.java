package ru.ulstu.teacher_service.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssignSubjectRequest(
        @NotBlank @Size(max = 255) String subject) {
}