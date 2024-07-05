package ru.practicum.shareit.request.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestJpaRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByApplicantId(Long userId, Sort sort);

    List<ItemRequest> findByApplicantIdNot(Long userId, Pageable pageable);
}
