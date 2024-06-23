package ru.practicum.shareit.request.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.EntityNotExistsExeption;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private User user;
    private ItemRequestCreateDto itemRequestCreateDto;
    private ItemRequestResponseDto itemRequestResponseDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("testUser")
                .email("testUser@example.com")
                .build();
        user.setId(1L);

        itemRequestCreateDto = ItemRequestCreateDto.builder()
                .description("Need a laptop")
                .build();

        itemRequestResponseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a laptop")
                .build();
    }

    @Test
    void createItemRequest_shouldReturnCreatedRequest() throws Exception {
        when(itemRequestService.createItemRequest(anyLong(), any(ItemRequestCreateDto.class)))
                .thenReturn(itemRequestResponseDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(itemRequestResponseDto.getDescription())));
    }

    @Test
    void getAllRequestsForUser_shouldReturnListOfRequests() throws Exception {
        List<ItemRequestResponseDto> requests = Collections.singletonList(itemRequestResponseDto);
        when(itemRequestService.getAllRequestsForUser(anyLong()))
                .thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$[0].description", is(itemRequestResponseDto.getDescription())));
    }

    @Test
    void getAllRequests_shouldReturnListOfRequests() throws Exception {
        List<ItemRequestResponseDto> requests = Collections.singletonList(itemRequestResponseDto);
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$[0].description", is(itemRequestResponseDto.getDescription())));
    }

    @Test
    void getItemRequest_shouldReturnItemRequest() throws Exception {
        when(itemRequestService.getItemRequest(anyLong(), anyLong()))
                .thenReturn(itemRequestResponseDto);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(itemRequestResponseDto.getDescription())));
    }

    @Test
    void getItemRequest_shouldReturnNotFound_whenRequestNotExists() throws Exception {
        EntityNotExistsExeption entityNotExistsExeption = new EntityNotExistsExeption("Запроса 1 не существует");
        when(itemRequestService.getItemRequest(anyLong(), anyLong()))
                .thenThrow(entityNotExistsExeption);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(entityNotExistsExeption.toString())));
    }
}
