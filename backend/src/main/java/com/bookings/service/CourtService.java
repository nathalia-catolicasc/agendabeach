package com.bookings.service;

import com.bookings.dto.CourtResponseDTO;
import com.bookings.dto.CreateCourtDTO;
import com.bookings.dto.UpdateCourtDTO;
import com.bookings.entity.Court;
import com.bookings.enums.CourtStatus;
import com.bookings.repository.CourtRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CourtService {

    static final String COURT_NOT_FOUND = "Quadra não encontrada";

    private final CourtRepository courtRepository;

    public CourtService(CourtRepository courtRepository) {
        this.courtRepository = courtRepository;
    }

    public CourtResponseDTO create(CreateCourtDTO dto) {
        Court court = new Court();
        court.setName(dto.name());
        court.setType(dto.type());
        court.setStatus(dto.status() != null ? dto.status() : CourtStatus.ACTIVE);
        return response(courtRepository.save(court));
    }

    public List<CourtResponseDTO> listAll() {
        return courtRepository.findAll().stream().map(CourtService::response).toList();
    }

    public CourtResponseDTO getById(Long id) {
        return response(courtRepository.findById(id).orElseThrow(CourtService::notFound));
    }

    @Transactional
    public CourtResponseDTO update(Long id, UpdateCourtDTO dto) {
        Court court = courtRepository.lockById(id).orElseThrow(CourtService::notFound);
        if (dto.name() != null && !dto.name().isBlank()) {
            court.setName(dto.name());
        }
        if (dto.type() != null) {
            court.setType(dto.type());
        }
        if (dto.status() != null) {
            court.setStatus(dto.status());
        }
        return response(courtRepository.save(court));
    }

    public void delete(Long id) {
        if (!courtRepository.existsById(id)) {
            throw notFound();
        }
        courtRepository.deleteById(id);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, COURT_NOT_FOUND);
    }

    private static CourtResponseDTO response(Court court) {
        return new CourtResponseDTO(
                court.getId(), court.getName(), court.getType(), court.getStatus());
    }
}
