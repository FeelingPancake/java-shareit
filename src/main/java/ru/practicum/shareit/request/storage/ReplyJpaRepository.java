package ru.practicum.shareit.request.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.Reply;

public interface ReplyJpaRepository extends JpaRepository<Reply, Long> {
}
