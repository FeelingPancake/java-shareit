package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.utils.annotations.Marker;


@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    @NotBlank(groups = Marker.OnCreate.class, message = "Имя не должно быть пустым")
    String name;
    @Email(message = "Email должен быть формата 'example@mail.com'")
    @NotBlank(groups = Marker.OnCreate.class, message = "Почта должна быть указана")
    String email;
}
