package br.com.alura.runnercircleapi.controller;

import br.com.alura.runnercircleapi.dto.LoginRequestDTO;
import br.com.alura.runnercircleapi.dto.LoginResponseDTO;
import br.com.alura.runnercircleapi.dto.RegisterRequestDTO;
import br.com.alura.runnercircleapi.dto.UserResponseDTO;
import br.com.alura.runnercircleapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Cadastro e login de usuários do Runner Circle")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Cadastra um novo usuário")
    public ResponseEntity<UserResponseDTO> registrar(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica o usuário e devolve um token JWT")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return authService.login(dto);
    }
}
