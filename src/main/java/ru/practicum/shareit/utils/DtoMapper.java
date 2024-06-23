package ru.practicum.shareit.utils;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOwner;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ReplyDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.Reply;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public abstract class DtoMapper {
    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .status(booking.getStatus())
                .booker(new BookingDto.Booker(booking.getBooker().getId()))
                .item(new BookingDto.ItemResponse(booking.getItem().getId(), booking.getItem().getName()))
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getUser().getName())
                .created(comment.getCreatedAt())
                .build();
    }

    public static ItemDto toItemDto(Item item, User user, Long requestId) {
        return ItemDto.builder()
                .id(item.getId())
                .owner(user)
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(requestId)
                .build();
    }

    public static ItemDtoRequest toRequest(Item item) {
        return ItemDtoRequest.builder()
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static ItemDtoOwner toItemDtoOwner(Item item,
                                              ItemDtoOwner.LastBooking lastBooking,
                                              ItemDtoOwner.NextBooking nextBooking,
                                              List<CommentDto> comments) {
        return ItemDtoOwner.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments)
                .build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static ItemRequest toItemRequestEntity(User applicant, ItemRequestCreateDto itemRequestCreateDto) {
        return ItemRequest.builder()
                .applicant(applicant)
                .description(itemRequestCreateDto.getDescription())
                .build();
    }

    public static ItemRequestResponseDto toItemRequestResponseDto(ItemRequest itemRequest, List<ReplyDto> replyDtoList) {
        return ItemRequestResponseDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreatedAt())
                .items(replyDtoList)
                .build();
    }

    public static ReplyDto toReplyDto(Reply reply) {
        return ReplyDto.builder()
                .id(reply.getItem().getId())
                .name(reply.getItem().getName())
                .description(reply.getItem().getDescription())
                .requestId(reply.getItemRequest().getId())
                .available(reply.getItem().getAvailable())
                .build();
    }
}
