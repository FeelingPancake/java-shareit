package ru.practicum.shareit.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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
import ru.practicum.shareit.utils.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class DtoMapperTest {

    @Test
    void toBookingDto_shouldMapCorrectly() {
        User booker = User.builder().id(1L).name("booker").build();

        Item item = Item.builder().id(1L).name("Item").build();

        Booking booking = Booking.builder()
                .id(1L)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(item)
                .build();

        BookingDto bookingDto = DtoMapper.toBookingDto(booking);

        assertThat(bookingDto).isNotNull();
        assertThat(bookingDto.getId()).isEqualTo(booking.getId());
        assertThat(bookingDto.getStart()).isEqualTo(booking.getStartDate());
        assertThat(bookingDto.getEnd()).isEqualTo(booking.getEndDate());
        assertThat(bookingDto.getStatus()).isEqualTo(booking.getStatus());
        assertThat(bookingDto.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(bookingDto.getItem().getId()).isEqualTo(item.getId());
        assertThat(bookingDto.getItem().getName()).isEqualTo(item.getName());
    }

    @Test
    void toCommentDto_shouldMapCorrectly() {
        User user = User.builder().name("User").build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("CommentText")
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        CommentDto commentDto = DtoMapper.toCommentDto(comment);

        assertThat(commentDto).isNotNull();
        assertThat(commentDto.getId()).isEqualTo(comment.getId());
        assertThat(commentDto.getText()).isEqualTo(comment.getText());
        assertThat(commentDto.getAuthorName()).isEqualTo(user.getName());
        assertThat(commentDto.getCreated()).isEqualTo(comment.getCreatedAt());
    }

    @Test
    void toItemDto_shouldMapCorrectly() {
        User user = User.builder().id(1L).build();

        Item item = Item.builder().id(1L).name("Item").description("Description").available(true).build();

        Long requestId = 1L;
        List<Comment> comments = Collections.singletonList(Comment.builder().id(1L).user(user).item(item).text("Normal").build());
        ItemDto itemDto = DtoMapper.toItemDto(item, user, requestId, comments);

        assertThat(itemDto).isNotNull();
        assertThat(itemDto.getId()).isEqualTo(item.getId());
        assertThat(itemDto.getName()).isEqualTo(item.getName());
        assertThat(itemDto.getDescription()).isEqualTo(item.getDescription());
        assertThat(itemDto.getAvailable()).isEqualTo(item.getAvailable());
        assertThat(itemDto.getRequestId()).isEqualTo(requestId);
        assertThat(itemDto.getOwner().getId()).isEqualTo(user.getId());
        assertThat(itemDto.getComments().get(0).getAuthorName()).isEqualTo(user.getName());
    }

    @Test
    void toRequest_shouldMapCorrectly() {
        Item item = Item.builder().name("Item").description("description").available(true).build();

        ItemDtoRequest itemDtoRequest = DtoMapper.toRequest(item);

        assertThat(itemDtoRequest).isNotNull();
        assertThat(itemDtoRequest.getName()).isEqualTo(item.getName());
        assertThat(itemDtoRequest.getDescription()).isEqualTo(item.getDescription());
        assertThat(itemDtoRequest.getAvailable()).isEqualTo(item.getAvailable());
    }

    @Test
    void toItemDtoOwner_shouldMapCorrectly() {
        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true).build();

        ItemDtoOwner.LastBooking lastBooking = new ItemDtoOwner.LastBooking() {
            @Override
            public Long getid() {
                return 1L;
            }

            @Override
            public Long getBookerId() {
                return 1L;
            }
        };

        ItemDtoOwner.NextBooking nextBooking = new ItemDtoOwner.NextBooking() {
            @Override
            public Long getid() {
                return 2L;
            }

            @Override
            public Long getBookerId() {
                return 2L;
            }
        };

        List<CommentDto> comments = Collections.emptyList();

        ItemDtoOwner itemDtoOwner = DtoMapper.toItemDtoOwner(item, lastBooking, nextBooking, comments);

        assertThat(itemDtoOwner).isNotNull();
        assertThat(itemDtoOwner.getId()).isEqualTo(item.getId());
        assertThat(itemDtoOwner.getName()).isEqualTo(item.getName());
        assertThat(itemDtoOwner.getDescription()).isEqualTo(item.getDescription());
        assertThat(itemDtoOwner.getAvailable()).isEqualTo(item.getAvailable());
        assertThat(itemDtoOwner.getLastBooking()).isEqualTo(lastBooking);
        assertThat(itemDtoOwner.getNextBooking()).isEqualTo(nextBooking);
        assertThat(itemDtoOwner.getComments()).isEqualTo(comments);
    }

    @Test
    void toUserDto_shouldMapCorrectly() {
        User user = User.builder().name("User").email("user@example.com").build();

        UserDto userDto = DtoMapper.toUserDto(user);

        assertThat(userDto).isNotNull();
        assertThat(userDto.getName()).isEqualTo(user.getName());
        assertThat(userDto.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void toItemRequestEntity_shouldMapCorrectly() {
        User applicant = User.builder().id(1L).build();

        ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder().description("Слооооон").build();

        ItemRequest itemRequest = DtoMapper.toItemRequestEntity(applicant, itemRequestCreateDto);

        assertThat(itemRequest).isNotNull();
        assertThat(itemRequest.getApplicant()).isEqualTo(applicant);
        assertThat(itemRequest.getDescription()).isEqualTo(itemRequestCreateDto.getDescription());
    }

    @Test
    void toItemRequestResponseDto_shouldMapCorrectly() {
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Слооооны")
                .createdAt(LocalDateTime.now())
                .build();

        List<ReplyDto> replyDtoList = Collections.emptyList();

        ItemRequestResponseDto itemRequestResponseDto = DtoMapper.toItemRequestResponseDto(itemRequest, replyDtoList);

        assertThat(itemRequestResponseDto).isNotNull();
        assertThat(itemRequestResponseDto.getId()).isEqualTo(itemRequest.getId());
        assertThat(itemRequestResponseDto.getDescription()).isEqualTo(itemRequest.getDescription());
        assertThat(itemRequestResponseDto.getCreated()).isEqualTo(itemRequest.getCreatedAt());
        assertThat(itemRequestResponseDto.getItems()).isEqualTo(replyDtoList);
    }

    @Test
    void toReplyDto_shouldMapCorrectly() {
        Item item = Item.builder().id(1L)
                .name("Item")
                .description("description")
                .available(true)
                .build();

        ItemRequest itemRequest = ItemRequest.builder().id(1L).build();

        Reply reply = Reply.builder().itemRequest(itemRequest).item(item).build();

        ReplyDto replyDto = DtoMapper.toReplyDto(reply);

        assertThat(replyDto).isNotNull();
        assertThat(replyDto.getId()).isEqualTo(item.getId());
        assertThat(replyDto.getName()).isEqualTo(item.getName());
        assertThat(replyDto.getDescription()).isEqualTo(item.getDescription());
        assertThat(replyDto.getRequestId()).isEqualTo(itemRequest.getId());
        assertThat(replyDto.getAvailable()).isEqualTo(item.getAvailable());
    }
}
