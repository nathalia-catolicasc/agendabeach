package com.bookings.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.bookings.dto.UpdateCourtDTO;
import com.bookings.repository.CourtRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {
    @Mock private CourtRepository courtRepository;
    @InjectMocks private CourtService courtService;

    @Test
    void getById_ShouldReturn404_WhenCourtNotFound() {
        when(courtRepository.findById(7L)).thenReturn(Optional.empty());

        assertNotFound(assertThrows(ResponseStatusException.class, () -> courtService.getById(7L)));
    }

    @Test
    void update_ShouldReturn404_WhenCourtNotFound() {
        when(courtRepository.lockById(7L)).thenReturn(Optional.empty());

        assertNotFound(
                assertThrows(
                        ResponseStatusException.class,
                        () -> courtService.update(7L, new UpdateCourtDTO("A", null, null))));
        verify(courtRepository, never()).save(any());
    }

    @Test
    void delete_ShouldReturn404_WhenCourtNotFound() {
        when(courtRepository.existsById(7L)).thenReturn(false);

        assertNotFound(assertThrows(ResponseStatusException.class, () -> courtService.delete(7L)));
        verify(courtRepository, never()).deleteById(anyLong());
    }

    private static void assertNotFound(ResponseStatusException error) {
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        assertEquals(CourtService.COURT_NOT_FOUND, error.getReason());
    }
}
