package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.AlreadyExistsException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
@Slf4j
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> temporaryDb = new HashMap<>();
    private long systemId = 1L;

    @Override
    public Item get(Long id) {
        log.info("Получен запрос на получение вещи по id = {}", id);

        return temporaryDb.get(id);
    }

    @Override
    public List<Item> getAll() {
        log.info("Получен запрос на получение всех вещей");

        return new ArrayList<>(temporaryDb.values());
    }

    @Override
    public Item create(Item item) {
        Long id = systemId++;
        Item itemWithId = item.toBuilder().id(id).build();
        log.info("Получен запрос на создание вещи");

        if (temporaryDb.containsKey(id)) {
            log.warn("Вещь с id = {} уже существует", id);

            throw new AlreadyExistsException(id.toString());
        }

        log.info("Вещь с id = {} создана", id);
        temporaryDb.put(id, itemWithId);

        return temporaryDb.get(id);
    }

    @Override
    public Item update(Item item) {
        log.info("Получен запрос на обновление вещи с id = {}", item.getId());
        temporaryDb.put(item.getId(), item);

        return temporaryDb.get(item.getId());
    }

    @Override
    public boolean delete(Long id) {
        log.info("Получен запрос на удаление вещи по id = {}", id);
        Optional<Item> item = Optional.ofNullable(temporaryDb.remove(id));

        return item.isPresent();
    }
}
