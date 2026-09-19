package ru.ulstu.teacher_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import ru.ulstu.teacher_service.service.TeacherService;
import ru.ulstu.teacher_service.web.dto.AssignSubjectRequest;
import ru.ulstu.teacher_service.web.dto.FireTeacherRequest;
import ru.ulstu.teacher_service.web.dto.HireTeacherRequest;
import ru.ulstu.teacher_service.web.dto.TeacherDto;
import ru.ulstu.teacher_service.web.dto.TeacherReportDto;
import ru.ulstu.teacher_service.web.dto.UpdateTeacherRequest;

@RestController
@RequestMapping("/api/teachers")
@Tag(name = "Преподаватели", description = "Учёт преподавателей ВУЗа (вариант 14)")
public class TeacherController {

    private final TeacherService service;

    public TeacherController(TeacherService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Список преподавателей (с постраничным выводом)")
    public Page<TeacherDto> list(@PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(pageable).map(TeacherDto::of);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить преподавателя по идентификатору")
    public TeacherDto get(@PathVariable Long id) {
        return TeacherDto.of(service.getById(id));
    }

    @PostMapping
    @Operation(summary = "Принять человека на должность")
    public ResponseEntity<TeacherDto> hire(@Valid @RequestBody HireTeacherRequest request,
                                           UriComponentsBuilder uriBuilder) {
        TeacherDto created = TeacherDto.of(service.hire(request));
        return ResponseEntity
                .created(uriBuilder.path("/api/teachers/{id}").build(created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Отредактировать информацию о преподавателе")
    public TeacherDto update(@PathVariable Long id,
                             @Valid @RequestBody UpdateTeacherRequest request) {
        return TeacherDto.of(service.update(id, request));
    }

    @PostMapping("/{id}/fire")
    @Operation(summary = "Уволить преподавателя по причине")
    public TeacherDto fire(@PathVariable Long id,
                           @Valid @RequestBody FireTeacherRequest request) {
        return TeacherDto.of(service.fire(id, request));
    }

    @PostMapping("/{id}/assign-subject")
    @Operation(summary = "Назначить предмет для чтения")
    public TeacherDto assignSubject(@PathVariable Long id,
                                    @Valid @RequestBody AssignSubjectRequest request) {
        return TeacherDto.of(service.assignSubject(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить запись о преподавателе")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/report")
    @Operation(summary = "Отчёт: сколько работает, а сколько уволено")
    public TeacherReportDto report() {
        return service.buildReport();
    }
}