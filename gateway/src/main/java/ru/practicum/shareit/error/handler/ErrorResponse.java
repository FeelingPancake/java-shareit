package ru.practicum.shareit.error.handler;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public record ErrorResponse(String error) {
    public void log() {
        log.warn("Ошибка - {}", error);
    }
}

