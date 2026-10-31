package ru.ulstu.teacher_service.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.ulstu.mergesort.ParallelMergeSort;
import ru.ulstu.teacher_service.domain.Teacher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class SortedTeachersService {

    private final TeacherService teacherService;
    private final MeterRegistry meterRegistry;

    public SortedTeachersService(TeacherService teacherService, MeterRegistry meterRegistry) {
        this.teacherService = teacherService;
        this.meterRegistry = meterRegistry;
    }

    public Map<String, Object> getSortedTeachers(int threads) {
        List<Teacher> all = teacherService.findAll(Pageable.unpaged()).getContent();
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

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("originalIds", originalIds);
        response.put("sortedIds", result.sorted());
        response.put("threads", threads);
        response.put("size", originalIds.size());
        response.put("durationNanos", result.durationNanos());
        response.put("durationMillis", result.durationMillis());
        
        return response;
    }

    public List<Map<String, Object>> runBenchmark(int size, String threadsParam) {
        List<Long> data = new Random(42)
                .longs(size, 1, size * 10L)
                .boxed()
                .collect(Collectors.toList());

        List<Map<String, Object>> results = new ArrayList<>();
        for (String s : threadsParam.split(",")) {
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
            row.put("durationNanos", r.durationNanos());
            results.add(row);
        }
        return results;
    }
}