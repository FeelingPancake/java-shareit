package ru.practicum.shareit.request.integrity;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.EntityNotExistsExeption;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJpaRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class ItemRequestServiceIntegrityTest {

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ItemRequestJpaRepository itemRequestJpaRepository;

    private User user;
    private User anotherUser;
    private ItemRequestCreateDto itemRequestCreateDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("TestUser")
                .email("testUser@example.com")
                .build();
        user = userJpaRepository.save(user);

        anotherUser = User.builder()
                .name("anotherUser")
                .email("anotherUser@example.com")
                .build();
        anotherUser = userJpaRepository.save(anotherUser);

        itemRequestCreateDto = ItemRequestCreateDto.builder()
                .description("Хочу слона")
                .build();
    }

    @Test
    void createItemRequest_shouldCreateAndReturnItemRequest() {
        ItemRequestResponseDto responseDto = itemRequestService.createItemRequest(user.getId(), itemRequestCreateDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getDescription()).isEqualTo(itemRequestCreateDto.getDescription());

        ItemRequest savedRequest = itemRequestJpaRepository.findById(responseDto.getId()).orElse(null);
        assertThat(savedRequest).isNotNull();
        assertThat(savedRequest.getDescription()).isEqualTo(itemRequestCreateDto.getDescription());
    }

    @Test
    void getAllRequestsForUser_shouldReturnListOfRequests() {
        ItemRequestResponseDto responseDto = itemRequestService.createItemRequest(user.getId(), itemRequestCreateDto);

        List<ItemRequestResponseDto> requests = itemRequestService.getAllRequestsForUser(user.getId());
        assertThat(requests).isNotEmpty();
        assertThat(requests.get(0).getDescription()).isEqualTo(responseDto.getDescription());
    }

    @Test
    void getAllRequests_shouldReturnListOfRequests() {
        itemRequestService.createItemRequest(user.getId(), itemRequestCreateDto);

        ItemRequestCreateDto itemRequestCreateDto2 = itemRequestCreateDto.toBuilder().description("Хочу другого слона").build();
        itemRequestService.createItemRequest(anotherUser.getId(), itemRequestCreateDto2);

        List<ItemRequestResponseDto> requests = itemRequestService.getAllRequests(user.getId(), 0, 10);
        assertThat(requests).isNotEmpty();
        assertThat(requests.get(0).getDescription()).isEqualTo(itemRequestCreateDto2.getDescription());
    }

    @Test
    void getItemRequest_shouldReturnItemRequest() {
        ItemRequestResponseDto createdRequest = itemRequestService.createItemRequest(user.getId(), itemRequestCreateDto);

        ItemRequestResponseDto responseDto = itemRequestService.getItemRequest(user.getId(), createdRequest.getId());
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getDescription()).isEqualTo(createdRequest.getDescription());
    }

    @Test
    void getItemRequest_shouldThrowExceptionWhenUserNotFound() {
        Long invalidUserId = 999L;

        Exception exception = assertThrows(EntityNotExistsExeption.class, () -> {
            itemRequestService.getItemRequest(invalidUserId, 1L);
        });

        assertThat(exception.getMessage()).isEqualTo("Пользователя " + invalidUserId + "не существует");
    }

    @Test
    void getItemRequest_shouldThrowExceptionWhenRequestNotFound() {
        Long invalidRequestId = 999L;

        Exception exception = assertThrows(EntityNotExistsExeption.class, () -> {
            itemRequestService.getItemRequest(user.getId(), invalidRequestId);
        });

        assertThat(exception.getMessage()).isEqualTo("Запроса " + invalidRequestId + " не существует");
    }
}
