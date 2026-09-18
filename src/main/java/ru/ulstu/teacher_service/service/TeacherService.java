package ru.ulstu.teacher_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ulstu.teacher_service.domain.Teacher;
import ru.ulstu.teacher_service.domain.TeacherStatus;
import ru.ulstu.teacher_service.repository.TeacherRepository;
import ru.ulstu.teacher_service.web.dto.AssignSubjectRequest;
import ru.ulstu.teacher_service.web.dto.FireTeacherRequest;
import ru.ulstu.teacher_service.web.dto.HireTeacherRequest;
import ru.ulstu.teacher_service.web.dto.TeacherReportDto;
import ru.ulstu.teacher_service.web.dto.UpdateTeacherRequest;

import java.time.Instant;

@Service
@Transactional
public class TeacherService {

    private final TeacherRepository repository;

    public TeacherService(TeacherRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Teacher> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Teacher getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Преподаватель с id=" + id + " не найден"));
    }

    public Teacher hire(HireTeacherRequest request) {
        Teacher teacher = new Teacher();
        teacher.setFullName(request.fullName());
        teacher.setPosition(request.position());
        teacher.setStatus(TeacherStatus.WORKING);
        teacher.setHiredAt(Instant.now());
        return repository.save(teacher);
    }

    public Teacher update(Long id, UpdateTeacherRequest request) {
        Teacher teacher = getById(id);
        teacher.setFullName(request.fullName());
        teacher.setPosition(request.position());
        return teacher;
    }

    public Teacher fire(Long id, FireTeacherRequest request) {
        Teacher teacher = getById(id);
        if (teacher.getStatus() == TeacherStatus.FIRED) {
            throw new BusinessRuleException("Преподаватель уже уволен");
        }
        teacher.setStatus(TeacherStatus.FIRED);
        teacher.setFireReason(request.reason());
        teacher.setFiredAt(Instant.now());
        return teacher;
    }

    public Teacher assignSubject(Long id, AssignSubjectRequest request) {
        Teacher teacher = getById(id);
        if (teacher.getStatus() == TeacherStatus.FIRED) {
            throw new BusinessRuleException(
                    "Нельзя назначить предмет уволенному преподавателю");
        }
        teacher.setSubject(request.subject());
        return teacher;
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Преподаватель с id=" + id + " не найден");
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public TeacherReportDto buildReport() {
        long working = repository.countByStatus(TeacherStatus.WORKING);
        long fired = repository.countByStatus(TeacherStatus.FIRED);
        return new TeacherReportDto(working, fired, working + fired);
    }
}