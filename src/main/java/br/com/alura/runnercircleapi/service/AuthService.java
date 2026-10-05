package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.dto.LoginRequestDTO;
import br.com.alura.runnercircleapi.dto.LoginResponseDTO;
import br.com.alura.runnercircleapi.dto.RegisterRequestDTO;
import br.com.alura.runnercircleapi.dto.UserResponseDTO;
import br.com.alura.runnercircleapi.exception.CredenciaisInvalidasException;
import br.com.alura.runnercircleapi.exception.UsuarioJaExisteException;
import br.com.alura.runnercircleapi.mapper.UserMapper;
import br.com.alura.runnercircleapi.model.Role;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // Hash usado quando o email não existe, para que essa resposta demore o mesmo que uma senha errada
    // e não revele quais emails estão cadastrados.
    private String hashFicticio;

    @PostConstruct
    void gerarHashFicticio() {
        hashFicticio = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    public UserResponseDTO registrar(RegisterRequestDTO dto) {
        String username = dto.username().trim();
        String email = dto.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new UsuarioJaExisteException("username já está em uso");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UsuarioJaExisteException("email já está em uso");
        }

        User user = new User(dto.nome().trim(), username, email, passwordEncoder.encode(dto.senha()));
        user.setRole(Role.USER);
        user = userRepository.save(user);

        log.info("Usuário registrado: id={}", user.getId());
        return userMapper.toResponseDTO(user);
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Optional<User> encontrado = userRepository.findByEmailIgnoreCase(dto.email().trim());

        String hash = encontrado.map(User::getSenha).orElse(hashFicticio);
        boolean senhaConfere = passwordEncoder.matches(dto.senha(), hash);

        if (encontrado.isEmpty() || !senhaConfere) {
            throw new CredenciaisInvalidasException();
        }

        User user = encontrado.get();
        String token = jwtService.gerarToken(user, dto.deveLembrar());

        log.info("Login realizado: id={}, lembrarMe={}", user.getId(), dto.deveLembrar());
        return new LoginResponseDTO(token, user.getRole(), userMapper.toResponseDTO(user));
    }
}
