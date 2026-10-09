package ru.ulstu.file_service.service;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}