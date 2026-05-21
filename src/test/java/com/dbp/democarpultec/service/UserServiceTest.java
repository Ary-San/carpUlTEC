package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.UserRequestDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserWhenValidData() {

        UserRequestDto dto = UserRequestDto.builder()
                .name("Juan")
                .lastName("Espinoza")
                .email("juan@test.com")
                .build();

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Juan");
        savedUser.setLastName("Espinoza");
        savedUser.setEmail("juan@test.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto result = userService.create(dto);

        assertNotNull(result);
        assertEquals("Juan", result.getName());
        assertEquals("juan@test.com", result.getEmail());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldReturnUserWhenIdExists() {

        User user = new User();
        user.setId(1L);
        user.setName("Juan");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto result = userService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Juan", result.getName());

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            userService.findById(99L);
        });

        verify(userRepository).findById(99L);
    }

    @Test
    void shouldReturnAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("Juan");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Maria");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponseDto> result = userService.findAll();

        assertEquals(2, result.size());
        assertEquals("Juan", result.get(0).getName());
        assertEquals("Maria", result.get(1).getName());

        verify(userRepository).findAll();
    }

    @Test
    void shouldUpdateUserWhenIdExists(){
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Juan");

        UserRequestDto dto = UserRequestDto.builder()
                .name("Carlos")
                .lastName("Lopez")
                .email("carlos@test.com")
                .build();

        User updateUser = new User();
        updateUser.setId(1L);
        updateUser.setName("Carlos");
        updateUser.setLastName("Lopez");
        updateUser.setEmail("carlos@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updateUser);

        UserResponseDto result = userService.update(1L, dto);

        assertEquals("Carlos", result.getName());
        assertEquals("Lopez", result.getLastName());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldDeleteUserWhenIdExists(){
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.delete(1L);
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingUserThatDoesNotExist() {

        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            userService.delete(99L);
        });

        verify(userRepository, never()).deleteById(anyLong());
    }
}