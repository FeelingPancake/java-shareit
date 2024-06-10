package ru.practicum.shareit.user.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;

public interface UserJpaRepository extends JpaRepository<User, Long> {
}
