package ru.ulstu.teacher_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "teacher")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "position", nullable = false, length = 128)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TeacherStatus status = TeacherStatus.WORKING;

    @Column(name = "fire_reason", length = 255)
    private String fireReason;

    @Column(name = "subject", length = 255)
    private String subject;

    @Column(name = "hired_at")
    private Instant hiredAt;

    @Column(name = "fired_at")
    private Instant firedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public TeacherStatus getStatus() { return status; }
    public void setStatus(TeacherStatus status) { this.status = status; }

    public String getFireReason() { return fireReason; }
    public void setFireReason(String fireReason) { this.fireReason = fireReason; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Instant getHiredAt() { return hiredAt; }
    public void setHiredAt(Instant hiredAt) { this.hiredAt = hiredAt; }

    public Instant getFiredAt() { return firedAt; }
    public void setFiredAt(Instant firedAt) { this.firedAt = firedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}