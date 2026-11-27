package com.users.service;

import com.users.dto.CreateUserDTO;
import com.users.dto.LoginDTO;
import com.users.dto.UpdateUserDTO;
import com.users.dto.UserResponseDTO;
import com.users.entity.User;
import com.users.enums.ProfileType;
import com.users.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {
    static final String INVALID_CREDENTIALS = "E-mail ou senha inválidos";
    static final String USER_NOT_FOUND = "Usuário não encontrado";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO create(CreateUserDTO dto) {
        User user = new User();
        user.setCpf(CpfValidator.normalize(dto.cpf()));
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setProfile(ProfileType.CLIENT);
        return response(userRepository.save(user));
    }

    public UserResponseDTO login(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.email()).orElseThrow(UserService::invalidLogin);
        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw invalidLogin();
        }
        return response(user);
    }

    public List<UserResponseDTO> listAll() {
        return userRepository.findAll().stream().map(UserService::response).toList();
    }

    public UserResponseDTO getById(Long id) {
        return response(userRepository.findById(id).orElseThrow(UserService::notFound));
    }

    @Transactional
    public UserResponseDTO update(Long id, UpdateUserDTO dto) {
        User user = userRepository.lockById(id).orElseThrow(UserService::notFound);

        if (dto.cpf() != null) {
            String cpf = CpfValidator.normalize(dto.cpf());
            if (user.getCpf() != null && !user.getCpf().equals(cpf))
                throw new IllegalArgumentException("O CPF cadastrado não pode ser alterado");
            user.setCpf(cpf);
        }
        if (dto.name() != null && !dto.name().isBlank()) {
            user.setName(dto.name());
        }
        if (dto.email() != null && !dto.email().isBlank()) {
            user.setEmail(dto.email());
        }
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }
        return response(userRepository.save(user));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw notFound();
        }
        userRepository.deleteById(id);
    }

    private static ResponseStatusException invalidLogin() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND);
    }

    private static UserResponseDTO response(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getProfile());
    }
}
