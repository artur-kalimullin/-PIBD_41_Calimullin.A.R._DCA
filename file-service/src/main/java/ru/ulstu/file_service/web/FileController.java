package ru.ulstu.file_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ulstu.file_service.domain.StoredFileInfo;
import ru.ulstu.file_service.service.FileStorageService;
import ru.ulstu.file_service.web.dto.UploadFileRequest;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Файлы", description = "Файловое хранилище")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileStorageService service;

    public FileController(FileStorageService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Загрузить файл (содержимое в base64)")
    public StoredFileInfo upload(@Valid @RequestBody UploadFileRequest request) {
        log.info("POST /api/files name={}", request.filename());
        byte[] bytes = Base64.getDecoder().decode(request.content());
        return service.store(request.filename(), bytes);
    }

    @GetMapping
    @Operation(summary = "Список файлов")
    public List<StoredFileInfo> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Скачать файл")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        StoredFileInfo info = service.getInfo(id);
        byte[] content = service.load(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + info.originalName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(content.length)
                .body(new ByteArrayResource(content));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить файл")
    public void delete(@PathVariable String id) {
        log.info("DELETE /api/files/{}", id);
        service.delete(id);
    }
}