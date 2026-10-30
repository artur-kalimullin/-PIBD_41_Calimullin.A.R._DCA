package ru.ulstu.teacher_service.web;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.ulstu.mergesort.ParallelMergeSort;
import ru.ulstu.teacher_service.domain.Teacher;
import ru.ulstu.teacher_service.service.TeacherService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teachers/sorted")
@Tag(name = "Параллельная сортировка", description = "Сортировка идентификаторов преподавателей")
public class SortedTeachersController {

    private static final Logger log = LoggerFactory.getLogger(SortedTeachersController.class);

    private final TeacherService service;
    private final MeterRegistry meterRegistry;

    public SortedTeachersController(TeacherService service, MeterRegistry meterRegistry) {
        this.service = service;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping
    @Operation(summary = "Отсортировать идентификаторы преподавателей")
    public Map<String, Object> sortedTeachers(
            @RequestParam(defaultValue = "4") int threads) {

        log.info("GET /api/teachers/sorted threads={}", threads);

        List<Teacher> all = service.findAll(Pageable.unpaged()).getContent();
        List<Long> originalIds = all.stream()
                .map(Teacher::getId)
                .collect(Collectors.toList());

        Timer timer = Timer.builder("sort.teachers.duration")
                .description("Время сортировки списка преподавателей")
                .tag("threads", String.format("%02d", threads))
                .register(meterRegistry);

        ParallelMergeSort.SortResult<Long> result = timer.record(() -> 
                ParallelMergeSort.sort(originalIds, threads)
        );

        log.info("Сортировка: n={} threads={} duration={}ms",
                originalIds.size(), threads, result.durationMillis());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("originalIds",  originalIds);
        response.put("sortedIds",    result.sorted());
        response.put("threads",      threads);
        response.put("size",         originalIds.size());
        response.put("durationNanos", result.durationNanos());
        response.put("durationMillis", result.durationMillis());
        return response;
    }

    @GetMapping("/benchmark")
    @Operation(summary = "Бенчмарк: сравнить время сортировки при разном числе потоков")
    public List<Map<String, Object>> benchmark(
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(defaultValue = "1,2,4,8,16") String threads) {

        List<Long> data = new Random(42)
                .longs(size, 1, size * 10L)
                .boxed()
                .collect(Collectors.toList());

        List<Map<String, Object>> results = new ArrayList<>();
        for (String s : threads.split(",")) {
            int t = Integer.parseInt(s.trim());
            
            Timer timer = Timer.builder("sort.benchmark.duration")
                    .description("Время сортировки синтетического массива")
                    .tag("threads", String.valueOf(t))
                    .register(meterRegistry);

            ParallelMergeSort.SortResult<Long> r = timer.record(() -> 
                    ParallelMergeSort.sort(data, t)
            );
            
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("threads", t);
            row.put("size", size);
            row.put("durationMillis", r.durationMillis());
            row.put("durationNanos",  r.durationNanos());
            results.add(row);
        }
        return results;
    }
}