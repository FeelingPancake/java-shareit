package ru.practicum.shareit.booking.endpoint;

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
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.EntityNotExistsExeption;
import ru.practicum.shareit.error.PermissionException;
import ru.practicum.shareit.error.handler.ErrorResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.DtoMapper;
import ru.practicum.shareit.utils.enums.State;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private Booking booking;
    private BookingDto bookingDto;
    private User booker;
    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        booker = User.builder()
                .name("Test Booker")
                .email("booker@example.com")
                .build();

        owner = User.builder()
                .name("Test Owner")
                .email("owner@example.com")
                .build();

        item = Item.builder()
                .name("Test Item")
                .description("Test Description")
                .owner(owner)
                .available(true)
                .build();

        booking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(booker)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();

        bookingDto = DtoMapper.toBookingDto(booking);
    }

    @Test
    void testGetBooking_success() throws Exception {
        when(bookingService.getBooking(anyLong(), anyLong())).thenReturn(booking);

        mockMvc.perform(get("/bookings/" + 1)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testGetBooking_failedUserNotFound() throws Exception {
        EntityNotExistsExeption entityNotExistsExeption = new EntityNotExistsExeption("1");
        when(bookingService.getBooking(anyLong(), anyLong())).thenThrow(entityNotExistsExeption);

        mockMvc.perform(get("/bookings/" + 1)
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(new ErrorResponse(entityNotExistsExeption.toString()).getError()));
    }

    @Test
    void testGetBooking_failedUserNotOwnsAndNotBooker() throws Exception {
        PermissionException permissionException = new PermissionException("1");
        when(bookingService.getBooking(anyLong(), anyLong())).thenThrow(permissionException);

        mockMvc.perform(get("/bookings/" + 1)
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(new ErrorResponse(permissionException.toString()).getError()));
    }

    @Test
    void testGetBookingsForBooker_success() throws Exception {
        when(bookingService.getBookingsForBooker(anyLong(), any(State.class), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(booking));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "REJECTED")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testGetBookingsForBooker_successWithoutParams() throws Exception {
        when(bookingService.getBookingsForBooker(anyLong(), any(State.class), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(booking));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testGetBookingsForBooker_failedUserNotExists() throws Exception {
        EntityNotExistsExeption entityNotExistsExeption = new EntityNotExistsExeption("1");

        when(bookingService.getBookingsForBooker(anyLong(), any(State.class), anyInt(), anyInt()))
                .thenThrow(entityNotExistsExeption);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "REJECTED")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(new ErrorResponse(entityNotExistsExeption.toString()).getError()));
    }

    @Test
    void testGetBookingsForBooker_failedStateUnknown() throws Exception {
        UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("1");

        when(bookingService.getBookingsForBooker(anyLong(), any(State.class), anyInt(), anyInt()))
                .thenThrow(unsupportedOperationException);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "REJECTED")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(new ErrorResponse(unsupportedOperationException.toString()).getError()));
    }

    @Test
    void testGetBookingsForBooker_failedNegativeParam() throws Exception {

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "REJECTED")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").hasJsonPath());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "REJECTED")
                        .param("from", "1")
                        .param("size", "-10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").hasJsonPath());
    }

    @Test
    void testGetBookingsForOwner_success() throws Exception {
        when(bookingService.getBookingsForOwner(anyLong(), any(State.class), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(booking));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "REJECTED")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testGetBookingsForOwner_successWithoutParams() throws Exception {
        when(bookingService.getBookingsForOwner(anyLong(), any(State.class), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(booking));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testGetBookingsForOwner_failedUserNotExists() throws Exception {
        EntityNotExistsExeption entityNotExistsExeption = new EntityNotExistsExeption("1");

        when(bookingService.getBookingsForOwner(anyLong(), any(State.class), anyInt(), anyInt()))
                .thenThrow(entityNotExistsExeption);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "REJECTED")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(new ErrorResponse(entityNotExistsExeption.toString()).getError()));
    }

    @Test
    void testGetBookingsForOwner_failedStateUnknown() throws Exception {
        UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("1");

        when(bookingService.getBookingsForOwner(anyLong(), any(State.class), anyInt(), anyInt()))
                .thenThrow(unsupportedOperationException);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "REJECTED")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(new ErrorResponse(unsupportedOperationException.toString()).getError()));
    }

    @Test
    void testGetBookingsForOwner_failedNegativeParam() throws Exception {

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "REJECTED")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").hasJsonPath());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "REJECTED")
                        .param("from", "1")
                        .param("size", "-10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").hasJsonPath());
    }

    @Test
    void testAddBooking_success() throws Exception {
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setItemId(1L);
        bookingRequest.setStart(LocalDateTime.now().plusDays(1));
        bookingRequest.setEnd(LocalDateTime.now().plusDays(2));

        booking.setId(1L);
        when(bookingService.create(anyLong(), any(BookingRequest.class))).thenReturn(booking);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .content(objectMapper.writeValueAsString(bookingRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.item.name").value("Test Item"));
    }

    @Test
    void testAddBooking_failedBecauseUserNotExists() throws Exception {
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setItemId(1L);
        bookingRequest.setStart(LocalDateTime.now().plusDays(1));
        bookingRequest.setEnd(LocalDateTime.now().plusDays(2));

        booking.setId(1L);
        EntityNotExistsExeption entityNotExistsExeption = new EntityNotExistsExeption("1");
        when(bookingService.create(anyLong(), any(BookingRequest.class))).thenThrow(entityNotExistsExeption);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(bookingRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(new ErrorResponse(entityNotExistsExeption.toString()).getError()));
    }

    @Test
    void testAddBooking_failedBecauseBookingRequestNotValid() throws Exception {
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setItemId(1L);
        bookingRequest.setStart(LocalDateTime.now().plusDays(2));
        bookingRequest.setEnd(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(bookingRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").hasJsonPath());

        bookingRequest.setItemId(null);
        bookingRequest.setStart(LocalDateTime.now().plusDays(1));
        bookingRequest.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(bookingRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").hasJsonPath());
    }

    @Test
    void testSetApprove() throws Exception {
        when(bookingService.setApprove(anyLong(), anyLong(), any(Boolean.class))).thenReturn(booking);

        mockMvc.perform(patch("/bookings/" + 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L));
    }
}

