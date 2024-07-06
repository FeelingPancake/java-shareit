package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.utils.annotations.Marker;

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
