package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.EntityNotExistsExeption;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJpaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserJpaRepository userStorage;

    @Transactional(readOnly = true)
    @Override
    public User getUser(Long id) {
        return userStorage.findById(id).orElseThrow(() -> new EntityNotExistsExeption(id.toString()));
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> getUsers() {

        return userStorage.findAll();
    }

    @Transactional
    @Override
    public User createUser(UserDto userDto) {
        User user = User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();

        return userStorage.save(user);
    }

    @Transactional
    @Override
    public User updateUser(UserDto userDto, Long id) {
        User user = userStorage.findById(id).orElseThrow(()
                -> new EntityNotExistsExeption(id.toString()));

        User userToUpdate = user.toBuilder()
                .name(userDto.getName() == null ? user.getName() : userDto.getName())
                .email(userDto.getEmail() == null ? user.getEmail() : userDto.getEmail())
                .build();

        return userStorage.save(userToUpdate);
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        userStorage.delete(userStorage.findById(id).orElseThrow(() -> new EntityNotExistsExeption(id.toString())));
    }
}
