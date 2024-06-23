package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.utils.annotations.Marker;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoRequest {
    @NotBlank(groups = {Marker.OnCreate.class}, message = "Имя вещи обязательна")
    String name;
    @Size(max = 255, message = "Описание не более 255 символов")
    @NotBlank(groups = {Marker.OnCreate.class}, message = "Описание вещи обязательна")
    String description;
    @NotNull(groups = {Marker.OnCreate.class}, message = "Доступность вещи обязательна")
    Boolean available;
    Long requestId;
}
