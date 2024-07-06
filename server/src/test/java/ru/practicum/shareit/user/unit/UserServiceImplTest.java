package ru.practicum.shareit.user.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.error.EntityNotExistsExeption;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserJpaRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserJpaRepository userStorage;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("hastur")
                .email("yellowKing@example.com")
                .build();

        userDto = UserDto.builder()
                .name("hastur")
                .email("yellowKing@example.com")
                .build();
    }

    @Test
    void getUser_shouldReturnUser_whenUserExists() {
        when(userStorage.findById(user.getId())).thenReturn(Optional.of(user));

        User result = userService.getUser(user.getId());

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        verify(userStorage, times(1)).findById(user.getId());
    }

    @Test
    void getUser_shouldThrowException_whenUserDoesNotExist() {
        when(userStorage.findById(user.getId())).thenReturn(Optional.empty());

        EntityNotExistsExeption exception = assertThrows(EntityNotExistsExeption.class, () -> userService.getUser(user.getId()));

        assertEquals(user.getId().toString(), exception.getMessage());
        verify(userStorage, times(1)).findById(user.getId());
    }

    @Test
    void getUsers_shouldReturnAllUsers() {
        List<User> users = List.of(user);
        when(userStorage.findAll()).thenReturn(users);

        List<User> result = userService.getUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).getId());
        verify(userStorage, times(1)).findAll();
    }

    @Test
    void createUser_shouldSaveAndReturnUser() {
        when(userStorage.save(any(User.class))).thenReturn(user);

        User result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
        verify(userStorage, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_shouldUpdateAndReturnUser_whenUserExists() {
        when(userStorage.findById(user.getId())).thenReturn(Optional.of(user));
        when(userStorage.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(userDto, user.getId());

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userStorage, times(1)).findById(user.getId());
        verify(userStorage, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowException_whenUserDoesNotExist() {
        when(userStorage.findById(user.getId())).thenReturn(Optional.empty());

        EntityNotExistsExeption exception = assertThrows(EntityNotExistsExeption.class, () -> userService.updateUser(userDto, user.getId()));

        assertEquals(user.getId().toString(), exception.getMessage());
        verify(userStorage, times(1)).findById(user.getId());
        verify(userStorage, times(0)).save(any(User.class));
    }

    @Test
    void deleteUser_shouldDeleteUser_whenUserExists() {
        userService.deleteUser(user.getId());

        verify(userStorage, times(1)).deleteById(user.getId());
    }

}
