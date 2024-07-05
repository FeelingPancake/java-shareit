package ru.practicum.shareit.user.integrity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserJpaRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
public class UserServiceIntegrityTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserJpaRepository userStorage;

    private User user;
    private UserDto userDto;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("Test")
                .email("test@example.com")
                .build();

        anotherUser = User.builder()
                .name("AnotherTest")
                .email("anotherTest@mail.com")
                .build();

        userDto = UserDto.builder()
                .name("Test")
                .email("test@example.com")
                .build();
    }

    @Test
    void getUser() {
        User savedUser = userStorage.save(user);

        User result = userService.getUser(savedUser.getId());

        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(savedUser.getName(), result.getName());
    }

    @Test
    void getUsers() {
        userStorage.save(user);
        userStorage.save(anotherUser);

        List<User> result = userService.getUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(user.getName(), result.get(0).getName());
    }

    @Test
    void createUser() {
        User result = userService.createUser(userDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());

        User savedUser = userStorage.findById(result.getId()).orElse(null);
        assertNotNull(savedUser);
        assertEquals(userDto.getName(), savedUser.getName());
        assertEquals(userDto.getEmail(), savedUser.getEmail());
    }

    @Test
    void updateUser() {
        User savedUser = userStorage.save(user);

        UserDto updatedUserDto = UserDto.builder()
                .name("Updated Name")
                .email("updated.email@example.com")
                .build();

        User result = userService.updateUser(updatedUserDto, savedUser.getId());

        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(updatedUserDto.getName(), result.getName());
        assertEquals(updatedUserDto.getEmail(), result.getEmail());

        User updatedUser = userStorage.findById(savedUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(updatedUserDto.getName(), updatedUser.getName());
        assertEquals(updatedUserDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void deleteUser() {
        User savedUser = userStorage.save(user);

        userService.deleteUser(savedUser.getId());

        User deletedUser = userStorage.findById(savedUser.getId()).orElse(null);
        assertNull(deletedUser);
    }

}
