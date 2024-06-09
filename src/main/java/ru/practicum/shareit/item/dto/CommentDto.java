package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Value;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;

@Value
@Builder
public class CommentDto {
    Long id;
    String text;
    String authorName;
    LocalDateTime created;

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getUser().getName())
                .created(comment.getCreatedAt())
                .build();
    }
}
