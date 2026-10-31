package ru.ulstu.teacher_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import ru.ulstu.teacher_service.service.SortedTeachersService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teachers/sorted")
@Tag(name = "Параллельная сортировка", description = "Сортировка идентификаторов преподавателей")
public class SortedTeachersController {

    private final SortedTeachersService sortedTeachersService;

    public SortedTeachersController(SortedTeachersService sortedTeachersService) {
        this.sortedTeachersService = sortedTeachersService;
    }

    @GetMapping
    @Operation(summary = "Отсортировать идентификаторы преподавателей")
    public Map<String, Object> sortedTeachers(@RequestParam(defaultValue = "4") int threads) {
        return sortedTeachersService.getSortedTeachers(threads);
    }

    @GetMapping("/benchmark")
    @Operation(summary = "Бенчмарк: сравнить время сортировки при разном числе потоков")
    public List<Map<String, Object>> benchmark(
            @RequestParam(defaultValue = "10000") int size,
            @RequestParam(defaultValue = "1,2,4,8,16") String threads) {
        return sortedTeachersService.runBenchmark(size, threads);
    }
}