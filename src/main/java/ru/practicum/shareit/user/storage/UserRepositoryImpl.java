package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.AlreadyExistsException;
import ru.practicum.shareit.error.UserDoesNotExixtsException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> temporaryDb = new HashMap<>();
    private long systemId = 1L;

    @Override
    public User get(Long id) {
        log.info("Запрос на получение пользователя по id = {}", id);
        Optional<User> optionalUser = Optional.ofNullable(temporaryDb.get(id));

        if (optionalUser.isEmpty()) {
            throw new UserDoesNotExixtsException(id.toString());
        }

        return optionalUser.get();
    }

    @Override
    public List<User> getAll() {
        log.info("Запрос на получение всех пользователей");
        return new ArrayList<>(temporaryDb.values());
    }

    @Override
    public User create(User user) {
        Long id = systemId++;
        User userWithId = user.toBuilder().id(id).build();
        log.info("Запрос на создание пользователя");
        if (temporaryDb.containsKey(id)) {
            log.warn("Пользователь с id = {} уже существует", id);
            throw new AlreadyExistsException(id.toString());
        }

        temporaryDb.put(id, userWithId);
        log.info("Пользователь с id = {} создан", id);

        return temporaryDb.get(id);
    }

    @Override
    public User update(User user) {
        Long id = user.getId();
        log.info("Запрос на обновление пользователя c id = {}", id);
        temporaryDb.put(id, user);
        log.info("Возвращение пользователя с id = {}", id);
        return temporaryDb.get(id);
    }

    @Override
    public boolean delete(Long id) {
        log.info("Запрос на удаление пользователя с id = {}", id);
        Optional<User> user = Optional.ofNullable(temporaryDb.remove(id));

        return user.isPresent();
    }
}
