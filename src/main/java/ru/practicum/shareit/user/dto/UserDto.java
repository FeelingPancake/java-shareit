package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Value;
import ru.practicum.shareit.annotations.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Value
@Builder(toBuilder = true)
public class UserDto {
    @NotBlank(groups = Marker.OnCreate.class, message = "Имя не должно быть пустым")
    String name;
    @Email(message = "Email должен быть формата 'example@mail.com'")
    @NotBlank(groups = Marker.OnCreate.class, message = "Почта должна быть указана")
    String email;
}
