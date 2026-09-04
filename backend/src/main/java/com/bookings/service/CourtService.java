package com.bookings.service;

import com.bookings.dto.CourtResponseDTO;
import com.bookings.dto.CreateCourtDTO;
import com.bookings.dto.UpdateCourtDTO;
import com.bookings.entity.Court;
import com.bookings.enums.CourtStatus;
import com.bookings.repository.CourtRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourtService {

    private final CourtRepository courtRepository;

    public CourtService(CourtRepository courtRepository) {
        this.courtRepository = courtRepository;
    }

    public CourtResponseDTO create(CreateCourtDTO dto) {
        Court court = new Court();
        court.setName(dto.name());
        court.setType(dto.type());
        court.setStatus(dto.status() != null ? dto.status() : CourtStatus.ACTIVE);

        Court saved = courtRepository.save(court);
        return new CourtResponseDTO(
                saved.getId(),
                saved.getName(),
                saved.getType(),
                saved.getStatus()
        );
    }

    public List<CourtResponseDTO> listAll() {
        return courtRepository.findAll().stream()
                .map(c -> new CourtResponseDTO(c.getId(), c.getName(), c.getType(), c.getStatus()))
                .collect(Collectors.toList());
    }

    public CourtResponseDTO getById(Long id) {
        Court c = courtRepository.findById(id).orElseThrow(() -> new RuntimeException("Court not found"));
        return new CourtResponseDTO(c.getId(), c.getName(), c.getType(), c.getStatus());
    }

    public CourtResponseDTO update(Long id, UpdateCourtDTO dto) {
        Court court = courtRepository.findById(id).orElseThrow(() -> new RuntimeException("Court not found"));
        if (dto.name() != null && !dto.name().isBlank()) {
            court.setName(dto.name());
        }
        if (dto.type() != null) {
            court.setType(dto.type());
        }
        if (dto.status() != null) {
            court.setStatus(dto.status());
        }
        Court saved = courtRepository.save(court);
        return new CourtResponseDTO(saved.getId(), saved.getName(), saved.getType(), saved.getStatus());
    }

    public void delete(Long id) {
        if (!courtRepository.existsById(id)) {
            throw new RuntimeException("Court not found");
        }
        courtRepository.deleteById(id);
    }
}
