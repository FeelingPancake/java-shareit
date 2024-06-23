package ru.practicum.shareit.booking.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.utils.enums.BookingStatus;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        BookingDto.Booker booker = new BookingDto.Booker(1L);
        BookingDto.ItemResponse itemResponse = new BookingDto.ItemResponse(1L, "ItemName");

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2023, 6, 1, 12, 0))
                .end(LocalDateTime.of(2023, 6, 2, 12, 0))
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(itemResponse)
                .build();
    }

    @Test
    void testSerialize() throws IOException {
        JsonContent<BookingDto> result = json.write(bookingDto);
        System.out.println(result);
        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2023-06-01T12:00:00");
        assertThat(result).hasJsonPathStringValue("$.end");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2023-06-02T12:00:00");
        assertThat(result).hasJsonPathStringValue("$.status");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
        assertThat(result).hasJsonPathMapValue("$.booker");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(result).hasJsonPathMapValue("$.item");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("ItemName");
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"id\":1," +
                "\"start\":\"2023-06-01T12:00:00\"," +
                "\"end\":\"2023-06-02T12:00:00\"," +
                "\"status\":\"APPROVED\"," +
                "\"booker\":{\"id\":1}," +
                "\"item\":{\"id\":1,\"name\":\"ItemName\"} }";

        BookingDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2023, 6, 1, 12, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2023, 6, 2, 12, 0));
        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(result.getBooker().getId()).isEqualTo(1);
        assertThat(result.getItem().getId()).isEqualTo(1);
        assertThat(result.getItem().getName()).isEqualTo("ItemName");
    }
}
