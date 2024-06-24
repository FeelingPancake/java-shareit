package ru.practicum.shareit.item.endpoint;

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
import ru.practicum.shareit.error.handler.ErrorResponse;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOwner;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private User itemOwner;
    private ItemDtoOwner itemDtoOwner;
    private ItemDtoRequest itemDtoRequest;

    @BeforeEach
    void setUp() {
        itemOwner = new User();
        itemOwner.setId(1L);

        itemDtoOwner = ItemDtoOwner.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        itemDtoRequest = ItemDtoRequest.builder()
                .name("NewItem")
                .description("New Description")
                .available(true)
                .build();
    }

    @Test
    void getItem_success() throws Exception {
        when(itemService.getItem(anyLong(), anyLong())).thenReturn(itemDtoOwner);

        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Item"))
                .andExpect(jsonPath("$.description").value("Description"));

        verify(itemService, times(1)).getItem(1L, 1L);
    }

    @Test
    void getItem_failedNotFound() throws Exception {
        EntityNotExistsExeption entityNotExistsExeption = new EntityNotExistsExeption("1");
        when(itemService.getItem(anyLong(), anyLong())).thenThrow(entityNotExistsExeption);

        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(new ErrorResponse(entityNotExistsExeption.toString()).getError()));

        verify(itemService, times(1)).getItem(1L, 1L);
    }

    @Test
    void getAllItems_success() throws Exception {
        List<ItemDtoOwner> items = Collections.singletonList(itemDtoOwner);
        when(itemService.getItems(anyLong(), anyInt(), anyInt())).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Item"));

        verify(itemService, times(1)).getItems(1L, 0, 10);
    }

    @Test
    void getAllItems_failedNotFound() throws Exception {
        EntityNotExistsExeption entityNotExistsExeption = new EntityNotExistsExeption("1");
        when(itemService.getItems(anyLong(), anyInt(), anyInt())).thenThrow(entityNotExistsExeption);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(new ErrorResponse(entityNotExistsExeption.toString()).getError()));

        verify(itemService, times(1)).getItems(1L, 0, 10);
    }

    @Test
    void getAllItems_failedNegativeParam() throws Exception {
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "-10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").hasJsonPath());
    }

    @Test
    void searchItems_success() throws Exception {
        List<ItemDto> items = Collections.singletonList(ItemDto.builder().build());
        when(itemService.findItems(anyString(), anyInt(), anyInt())).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "item")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(itemService, times(1)).findItems("item", 0, 10);
    }

    @Test
    void searchItems_FailedWhenTextEmpty() throws Exception {
        List<ItemDto> items = Collections.singletonList(ItemDto.builder().build());
        when(itemService.findItems(anyString(), anyInt(), anyInt())).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_FailedWhenParamIsNegative() throws Exception {
        List<ItemDto> items = Collections.singletonList(ItemDto.builder().build());
        when(itemService.findItems(anyString(), anyInt(), anyInt())).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "dddd")
                        .param("from", "-0")
                        .param("size", "-10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchItems_FailedWhenTextMissing() throws Exception {
        List<ItemDto> items = Collections.singletonList(ItemDto.builder().build());
        when(itemService.findItems(anyString(), anyInt(), anyInt())).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("from", "-0")
                        .param("size", "-10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createItem_success() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("NewItem")
                .description("New Description")
                .available(true)
                .build();

        when(itemService.createItem(any(ItemDtoRequest.class), anyLong())).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("NewItem"))
                .andExpect(jsonPath("$.description").value("New Description"));

        verify(itemService, times(1)).createItem(any(ItemDtoRequest.class), eq(1L));
    }

    @Test
    void createItem_NotFound() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("NewItem")
                .description("New Description")
                .available(true)
                .build();
        EntityNotExistsExeption entityNotExistsExeption = new EntityNotExistsExeption("1");
        when(itemService.createItem(any(ItemDtoRequest.class), anyLong())).thenThrow(entityNotExistsExeption);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(new ErrorResponse(entityNotExistsExeption.toString()).getError()));

        verify(itemService, times(1)).createItem(any(ItemDtoRequest.class), eq(1L));
    }

    @Test
    void createItem_FailedWhenDtoRequestWrong() throws Exception {
        ItemDtoRequest wrongItemDtoRequest = itemDtoRequest.toBuilder().available(null).description(null).name(null).build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongItemDtoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createComment_success() throws Exception {
        CommentDtoRequest commentDtoRequest = new CommentDtoRequest("Great item");
        Comment comment = Comment.builder()
                .text("GreatItem")
                .id(1L)
                .createdAt(LocalDateTime.now())
                .user(User.builder().name("TestUser").build())
                .build();

        when(itemService.createComment(anyLong(), any(CommentDtoRequest.class), anyLong())).thenReturn(comment);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDtoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("GreatItem"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.authorName").value("TestUser"))
                .andExpect(jsonPath("$.created").hasJsonPath());


        verify(itemService, times(1)).createComment(1L, commentDtoRequest, 1L);
    }

    @Test
    void createComment_failedNotFound() throws Exception {
        CommentDtoRequest commentDtoRequest = new CommentDtoRequest("Great item");
        EntityNotExistsExeption entityNotExistsExeption = new EntityNotExistsExeption("1");
        when(itemService.createComment(anyLong(), any(CommentDtoRequest.class), anyLong())).thenThrow(entityNotExistsExeption);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDtoRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(new ErrorResponse(entityNotExistsExeption.toString()).getError()));

        verify(itemService, times(1)).createComment(1L, commentDtoRequest, 1L);
    }

    @Test
    void createComment_failedWrondCommentDtoRequest() throws Exception {
        CommentDtoRequest commentDtoRequest = new CommentDtoRequest("");

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDtoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").hasJsonPath());
    }

    @Test
    void updateItem_success() throws Exception {
        ItemDto updatedItem = ItemDto.builder().id(1L).name("Updated Item").description("Updated Description").available(true).build();

        when(itemService.updateItem(any(ItemDtoRequest.class), anyLong(), anyLong())).thenReturn(updatedItem);

        mockMvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        ItemDto updatedItemWithOutDescription = ItemDto.builder().id(1L).name("Updated Item").available(true).build();

        when(itemService.updateItem(any(ItemDtoRequest.class), anyLong(), anyLong())).thenReturn(updatedItemWithOutDescription);

        mockMvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Item"));

        verify(itemService, times(2)).updateItem(any(ItemDtoRequest.class), eq(1L), eq(1L));
    }

    @Test
    void updateItem_failedNotFound() throws Exception {
        EntityNotExistsExeption entityNotExistsExeption = new EntityNotExistsExeption("1");
        when(itemService.updateItem(any(ItemDtoRequest.class), anyLong(), anyLong())).thenThrow(entityNotExistsExeption);

        mockMvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(new ErrorResponse(entityNotExistsExeption.toString()).getError()));

        verify(itemService, times(1)).updateItem(any(ItemDtoRequest.class), eq(1L), eq(1L));
    }

    @Test
    void deleteItem() throws Exception {
        mockMvc.perform(delete("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(itemService, times(1)).deleteItem(1L, 1L);
    }

    @Test
    void deleteItem_NotFound() throws Exception {
        EntityNotExistsExeption entityNotExistsExeption = new EntityNotExistsExeption("1");
        doThrow(entityNotExistsExeption).when(itemService).deleteItem(anyLong(), anyLong());

        mockMvc.perform(delete("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(new ErrorResponse(entityNotExistsExeption.toString()).getError()));


        verify(itemService, times(1)).deleteItem(1L, 1L);
    }
}
