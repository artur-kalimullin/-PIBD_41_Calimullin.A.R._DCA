package ru.ulstu.teacher_service.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTeacherRequest(
        @NotBlank @Size(max = 255) String fullName,
        @NotBlank @Size(max = 128) String position) {
}