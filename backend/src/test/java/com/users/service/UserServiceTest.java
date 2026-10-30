package com.users.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.users.dto.CreateUserDTO;
import com.users.dto.LoginDTO;
import com.users.dto.UpdateUserDTO;
import com.users.dto.UserResponseDTO;
import com.users.entity.User;
import com.users.enums.ProfileType;
import com.users.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String INVALID_CREDENTIALS_MSG = "Invalid email or password";
    private static final String NOT_FOUND_MSG = "User not found";

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private User buildUser(Long id, String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setProfile(ProfileType.CLIENT);
        if (id != null) {
            ReflectionTestUtils.setField(user, "id", id);
        }
        return user;
    }

    @Test
    void create_ShouldPersistUserAndReturnResponse() {
        CreateUserDTO dto =
                new CreateUserDTO("Ana", "ana@example.com", "password123", "529.982.247-25");

        when(passwordEncoder.encode("password123")).thenReturn("enc-password123");
        when(userRepository.save(any(User.class)))
                .thenAnswer(
                        inv -> {
                            User u = inv.getArgument(0);
                            ReflectionTestUtils.setField(u, "id", 1L);
                            return u;
                        });

        UserResponseDTO res = userService.create(dto);

        assertNotNull(res);
        assertEquals(1L, res.id());
        assertEquals("Ana", res.name());
        assertEquals("ana@example.com", res.email());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User persisted = captor.getValue();

        assertEquals("Ana", persisted.getName());
        assertEquals("ana@example.com", persisted.getEmail());
        assertEquals("enc-password123", persisted.getPassword());
        assertEquals("52998224725", persisted.getCpf());
        assertEquals(ProfileType.CLIENT, res.profile());
        assertEquals(ProfileType.CLIENT, persisted.getProfile());
    }

    @Test
    void login_ShouldReturnUser_WhenCredentialsValid() {
        User user = buildUser(1L, "Ana", "ana@example.com", "hash");
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123", "hash")).thenReturn(true);

        UserResponseDTO res = userService.login(new LoginDTO("ana@example.com", "123"));

        assertEquals(1L, res.id());
        assertEquals("Ana", res.name());
        assertEquals("ana@example.com", res.email());
        assertEquals(ProfileType.CLIENT, res.profile());
    }

    @Test
    void login_ShouldThrow_WhenPasswordIsWrong() {
        User user = buildUser(1L, "Ana", "ana@example.com", "hash");
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> userService.login(new LoginDTO("ana@example.com", "wrong")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals(INVALID_CREDENTIALS_MSG, ex.getReason());
    }

    @Test
    void login_ShouldThrow_WhenEmailNotFound() {
        when(userRepository.findByEmail("x@example.com")).thenReturn(Optional.empty());

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> userService.login(new LoginDTO("x@example.com", "123")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals(INVALID_CREDENTIALS_MSG, ex.getReason());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void listAll_ShouldReturnMappedUsers() {
        User u1 = buildUser(1L, "Ana", "ana@example.com", "hash1");
        User u2 = buildUser(2L, "Bruno", "bruno@example.com", "hash2");
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponseDTO> res = userService.listAll();

        assertEquals(2, res.size());
        assertEquals(1L, res.get(0).id());
        assertEquals("Ana", res.get(0).name());
        assertEquals("ana@example.com", res.get(0).email());
        assertEquals(2L, res.get(1).id());
        assertEquals("Bruno", res.get(1).name());
        assertEquals("bruno@example.com", res.get(1).email());
        assertEquals(ProfileType.CLIENT, res.get(0).profile());
        assertEquals(ProfileType.CLIENT, res.get(1).profile());
    }

    @Test
    void listAll_ShouldReturnEmptyList_WhenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponseDTO> res = userService.listAll();

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void getById_ShouldReturnUser_WhenFound() {
        User user = buildUser(1L, "Ana", "ana@example.com", "hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO res = userService.getById(1L);

        assertEquals(1L, res.id());
        assertEquals("Ana", res.name());
        assertEquals("ana@example.com", res.email());
        assertEquals(ProfileType.CLIENT, res.profile());
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.getById(1L));

        assertEquals(NOT_FOUND_MSG, ex.getMessage());
    }

    @Test
    void update_ShouldUpdateAllFields_WhenAllProvided() {
        User user = buildUser(1L, "Ana", "ana@example.com", "hash");
        when(userRepository.lockById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode("newpassword")).thenReturn("enc-newpassword");

        UserResponseDTO res =
                userService.update(
                        1L,
                        new UpdateUserDTO(
                                "Ana Maria",
                                "ana.maria@example.com",
                                "newpassword",
                                "529.982.247-25"));

        assertEquals(1L, res.id());
        assertEquals("Ana Maria", res.name());
        assertEquals("ana.maria@example.com", res.email());
        assertEquals("enc-newpassword", user.getPassword());
        assertEquals("52998224725", user.getCpf());
        assertEquals(ProfileType.CLIENT, res.profile());
        verify(userRepository).lockById(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository).save(user);
    }

    @Test
    void update_ShouldUpdateOnlyName_WhenOnlyNameProvided() {
        User user = buildUser(1L, "Ana", "ana@example.com", "hash");
        when(userRepository.lockById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO res =
                userService.update(1L, new UpdateUserDTO("Ana Maria", null, null, null));

        assertEquals("Ana Maria", res.name());
        assertEquals("ana@example.com", res.email());
        assertEquals("hash", user.getPassword());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void update_ShouldIgnoreNullAndBlankFields() {
        User user = buildUser(1L, "Ana", "ana@example.com", "hash");
        when(userRepository.lockById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO res = userService.update(1L, new UpdateUserDTO(null, "   ", "", null));

        assertEquals("Ana", res.name());
        assertEquals("ana@example.com", res.email());
        assertEquals("hash", user.getPassword());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void update_ShouldThrow_WhenUserNotFound() {
        when(userRepository.lockById(99L)).thenReturn(Optional.empty());

        RuntimeException ex =
                assertThrows(
                        RuntimeException.class,
                        () -> userService.update(99L, new UpdateUserDTO("Ana", null, null, null)));

        assertEquals(NOT_FOUND_MSG, ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void delete_ShouldDeleteUser_WhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrow_WhenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.delete(99L));

        assertEquals(NOT_FOUND_MSG, ex.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }
}
