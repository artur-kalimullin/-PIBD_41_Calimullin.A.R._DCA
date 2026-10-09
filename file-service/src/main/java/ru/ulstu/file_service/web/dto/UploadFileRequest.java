package ru.ulstu.file_service.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UploadFileRequest(
        @NotBlank @Size(max = 255) String filename,
        @NotBlank String content 
) {}