package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Value;
import ru.practicum.shareit.annotations.Marker;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Value
@Builder(toBuilder = true)
public class ItemRequest {
    @NotBlank(groups = {Marker.OnCreate.class}, message = "Имя вещи обязательна")
    String name;
    @Size(max = 255, message = "Описание не более 255 символов")
    @NotBlank(groups = {Marker.OnCreate.class}, message = "Описание вещи обязательна")
    String description;
    @NotNull(groups = {Marker.OnCreate.class}, message = "Доступность вещи обязательна")
    Boolean available;
}
