package ru.practicum.shareit.request.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "replies", schema = "public")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    ItemRequest itemRequest;
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    Item item;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;
}
