package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.AlreadyExistsException;
import ru.practicum.shareit.error.UserDoesNotExixtsException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userStorage;
    private long id;

    public UserServiceImpl(UserRepository userStorage) {
        this.userStorage = userStorage;
        this.id = 1L;
    }

    @Override
    public User getUser(Long id) {
        return userStorage.get(id);
    }

    @Override
    public List<User> getUsers() {

        return userStorage.getAll();
    }

    @Override
    public User createUser(UserDto userDto) {
        List<User> existingUsers = userStorage.getAll().stream()
                .filter(user -> user.getEmail().equals(userDto.getEmail()))
                .collect(Collectors.toList());

        if (!existingUsers.isEmpty()) {
            throw new AlreadyExistsException(userDto.getEmail());
        }

        User user = User.builder()
                .id(id++)
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();

        userStorage.create(user);

        return user;
    }

    @Override
    public User updateUser(UserDto userDto, Long id) {
        Optional<User> userOptional = Optional.ofNullable(userStorage.get(id));

        if (userOptional.isEmpty()) {
            throw new UserDoesNotExixtsException(id.toString());
        }

        List<User> existingUsers = userStorage.getAll().stream()
                .filter(user -> user.getEmail().equals(userDto.getEmail()))
                .collect(Collectors.toList());
        // Мне кажется костылем эта часть, так затем при добавление базы и сущностей уникальность почты будет намного проще
        if (!existingUsers.isEmpty() && !id.equals(existingUsers.get(0).getId())) {
            throw new AlreadyExistsException(userDto.getEmail());
        }

        User user = userOptional.get();
        User userToUpdate = user.toBuilder()
                .name(userDto.getName() == null ? user.getName() : userDto.getName())
                .email(userDto.getEmail() == null ? user.getEmail() : userDto.getEmail())
                .build();

        return userStorage.update(userToUpdate);
    }

    @Override
    public boolean deleteUser(Long id) {

        return userStorage.delete(id);
    }
}
