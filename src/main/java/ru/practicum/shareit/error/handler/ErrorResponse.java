package ru.practicum.shareit.error.handler;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

    @Value
    @Slf4j
    @AllArgsConstructor
public class ErrorResponse {
    String error;

    public void log() {
        log.warn("Ошибка - {}", error);
    }
}

