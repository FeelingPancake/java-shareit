package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User getUser(Long id);

    List<User> getUsers();

    User createUser(UserDto userDto);

    User updateUser(UserDto userDto, Long id);

    void deleteUser(Long id);
}
