package ru.practicum.shareit.request.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.error.EntityNotExistsExeption;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJpaRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestJpaRepository itemRequestStorage;
    @Mock
    private UserJpaRepository userJpaRepository;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void createItemRequest_success() {
        Long userId = 1L;
        User user = new User();
        ItemRequest itemRequest = new ItemRequest();
        ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder().description("testTesttest").build();
        itemRequestCreateDto.setDescription("Test Item Request");

        when(userJpaRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestStorage.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestResponseDto result = itemRequestService.createItemRequest(userId, itemRequestCreateDto);

        assertNotNull(result);
        verify(userJpaRepository, times(1)).findById(userId);
        verify(itemRequestStorage, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void createItemRequest_userNotFound() {
        Long userId = 1L;
        ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder().description("testTesttest").build();
        itemRequestCreateDto.setDescription("Test Item Request");

        when(userJpaRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotExistsExeption exception = assertThrows(EntityNotExistsExeption.class, () -> {
            itemRequestService.createItemRequest(userId, itemRequestCreateDto);
        });

        assertEquals("Пользователя " + userId + "нет", exception.getMessage());
        verify(userJpaRepository, times(1)).findById(userId);
        verify(itemRequestStorage, times(0)).save(any(ItemRequest.class));
    }

    @Test
    void getAllRequestsForUser_success() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        ItemRequest itemRequest1 = ItemRequest.builder()
                .id(1L)
                .applicant(user)
                .description("test1")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        ItemRequest itemRequest2 = ItemRequest.builder()
                .id(2L)
                .applicant(user)
                .description("test2")
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        List<ItemRequest> itemRequests = Arrays.asList(itemRequest1, itemRequest2);
        when(userJpaRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestStorage.findByApplicantId(userId, Sort.by(Sort.Direction.DESC, "createdAt"))).thenReturn(itemRequests);

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequestsForUser(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(itemRequest1.getDescription(), result.get(0).getDescription());
        assertEquals(itemRequest2.getDescription(), result.get(1).getDescription());

        verify(userJpaRepository, times(1)).findById(userId);
        verify(itemRequestStorage, times(1)).findByApplicantId(userId, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Test
    void getAllRequestsForUser_userNotFound() {
        Long userId = 1L;

        when(userJpaRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotExistsExeption.class, () -> itemRequestService.getAllRequestsForUser(userId));

        verify(userJpaRepository, times(1)).findById(userId);
        verify(itemRequestStorage, times(0)).findByApplicantId(anyLong(), any(Sort.class));
    }

    @Test
    void getAllRequests_success() {
        int from = 0;
        int size = 10;
        // int page = from / size; -> page = 0

        Pageable pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());

        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .build();

        ItemRequest itemRequest1 = ItemRequest.builder()
                .id(1L)
                .description("test1")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        ItemRequest itemRequest2 = ItemRequest.builder()
                .id(2L)
                .description("test2")
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        List<ItemRequest> itemRequests = Arrays.asList(itemRequest1, itemRequest2);

        when(itemRequestStorage.findByApplicantIdNot(anyLong(), any(Pageable.class))).thenReturn(itemRequests);

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequests(userId, from, size);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(itemRequest1.getDescription(), result.get(0).getDescription());
        assertEquals(itemRequest2.getDescription(), result.get(1).getDescription());

        verify(itemRequestStorage, times(1)).findByApplicantIdNot(anyLong(), any(Pageable.class));
    }

    @Test
    void getItemRequest_success() {
        Long requestId = 1L;
        Long userId = 1L;

        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("test1")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        User user = User.builder()
                .id(userId)
                .build();

        when(itemRequestStorage.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(userJpaRepository.findById(userId)).thenReturn(Optional.of(user));

        ItemRequestResponseDto result = itemRequestService.getItemRequest(userId, requestId);

        assertNotNull(result);
        assertEquals(itemRequest.getDescription(), result.getDescription());

        verify(itemRequestStorage, times(1)).findById(requestId);
    }

    @Test
    void getItemRequest_notFound() {
        Long requestId = 1L;
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .build();

        when(itemRequestStorage.findById(requestId)).thenReturn(Optional.empty());
        when(userJpaRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(EntityNotExistsExeption.class, () -> itemRequestService.getItemRequest(userId, requestId));

        verify(itemRequestStorage, times(1)).findById(requestId);
    }
}
