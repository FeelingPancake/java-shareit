package ru.practicum.shareit.request.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "requests", schema = "public")
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User applicant;
    @Column(name = "description", nullable = false)
    String description;
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
    @OneToMany(mappedBy = "itemRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Reply> replies;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
