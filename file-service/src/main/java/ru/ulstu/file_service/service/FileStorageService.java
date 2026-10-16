package ru.ulstu.file_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ulstu.file_service.domain.StoredFileInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import ru.ulstu.file_service.domain.PageResponse;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private static final String SEPARATOR = "___";

    private final Path storageRoot;

    public FileStorageService(@Value("${storage.path}") String storagePath) throws IOException {
        this.storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(this.storageRoot);
        log.info("File storage root: {}", this.storageRoot);
    }

    @CacheEvict(value = "filesList", allEntries = true)
    public StoredFileInfo store(String originalName, byte[] content) {
        String safeName = sanitize(originalName);
        String id = UUID.randomUUID().toString();
        Path target = storageRoot.resolve(id + SEPARATOR + safeName);
        try {
            Files.write(target, content, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить файл: " + e.getMessage(), e);
        }
        StoredFileInfo info = new StoredFileInfo(id, safeName, content.length, Instant.now());
        log.info("STORED id={} name={} size={}", id, safeName, content.length);
        return info;
    }

    public PageResponse<StoredFileInfo> listPaged(int page, int size) {
        if (page < 0) page = 0;
        if (size < 1 || size > 200) size = 20;

        List<StoredFileInfo> all = list();

        List<StoredFileInfo> sorted = all.stream()
                .sorted(Comparator.comparing(StoredFileInfo::storedAt).reversed())
                .toList();

        int from = Math.min(page * size, sorted.size());
        int to   = Math.min(from + size, sorted.size());
        List<StoredFileInfo> content = sorted.subList(from, to);

        int totalPages = (int) Math.ceil((double) sorted.size() / size);
        return new PageResponse<>(content, page, size, sorted.size(), totalPages);
    }

    @Cacheable(value = "filesList")
    public List<StoredFileInfo> list() {
        log.info("CACHE MISS: reading storage directory {}", storageRoot);
        try (Stream<Path> stream = Files.list(storageRoot)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(this::toInfo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать список файлов", e);
        }
    }

    public byte[] load(String id) {
        Path path = findById(id);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать файл " + id, e);
        }
    }

    public StoredFileInfo getInfo(String id) {
        return toInfo(findById(id));
    }

    @CacheEvict(value = "filesList", allEntries = true)
    public void delete(String id) {
        Path path = findById(id);
        try {
            Files.delete(path);
            log.info("DELETED id={}", id);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось удалить файл " + id, e);
        }
    }

    private Path findById(String id) {
        try (Stream<Path> stream = Files.list(storageRoot)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith(id + SEPARATOR))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Файл с id=" + id + " не найден"));
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка поиска файла", e);
        }
    }

    private StoredFileInfo toInfo(Path p) {
        String name = p.getFileName().toString();
        int idx = name.indexOf(SEPARATOR);
        if (idx < 0) return null;
        try {
            return new StoredFileInfo(
                    name.substring(0, idx),
                    name.substring(idx + SEPARATOR.length()),
                    Files.size(p),
                    Files.getLastModifiedTime(p).toInstant());
        } catch (IOException e) {
            return null;
        }
    }

    private String sanitize(String name) {
        if (name == null || name.isBlank()) return "file";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}