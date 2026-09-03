package com.users.service;

import com.users.dto.CreateUserDTO;
import com.users.dto.LoginDTO;
import com.users.dto.UserResponseDTO;
import com.users.entity.User;
import com.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "enc-" + invocation.getArgument(0));
    }

    @Test
    void create_ShouldPersistUserAndReturnResponse() {
        CreateUserDTO dto = new CreateUserDTO("Ana", "ana@example.com", "123");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            User saved = new User();
            saved.setEmail(u.getEmail());
            saved.setName(u.getName());
            saved.setPassword(u.getPassword());
            try {
                var idField = User.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(saved, 1L);
            } catch (Exception ignored) {}
            return saved;
        });

        UserResponseDTO res = userService.create(dto);

        assertNotNull(res);
        assertEquals(1L, res.id());
        assertEquals("Ana", res.name());
        assertEquals("ana@example.com", res.email());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertTrue(captor.getValue().getPassword().startsWith("enc-"));
    }

    @Test
    void login_ShouldReturnUser_WhenCredentialsValid() {
        User user = new User();
        user.setEmail("ana@example.com");
        user.setName("Ana");
        user.setPassword("hash");
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123", "hash")).thenReturn(true);

        UserResponseDTO res = userService.login(new LoginDTO("ana@example.com", "123"));

        assertEquals("Ana", res.name());
        assertEquals("ana@example.com", res.email());
    }

    @Test
    void login_ShouldThrow_WhenCredentialsInvalid() {
        User user = new User();
        user.setEmail("ana@example.com");
        user.setName("Ana");
        user.setPassword("hash");
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.login(new LoginDTO("ana@example.com", "wrong")));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
    }
}
