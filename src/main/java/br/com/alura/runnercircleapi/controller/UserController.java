package br.com.alura.runnercircleapi.controller;

import br.com.alura.runnercircleapi.dto.TreinoResponseDTO;
import br.com.alura.runnercircleapi.dto.UserAdminResponseDTO;
import br.com.alura.runnercircleapi.dto.UserResponseDTO;
import br.com.alura.runnercircleapi.dto.UserUpdateRequestDTO;
import br.com.alura.runnercircleapi.service.TreinoService;
import br.com.alura.runnercircleapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Endpoints de usuários do Runner Circle")
public class UserController {

    @Autowired
    private TreinoService treinoService;

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todas as pessoas usuárias (somente ADMIN)")
    public List<UserAdminResponseDTO> listar() {
        return userService.listarTodos();
    }

    @PutMapping("/me")
    @Operation(summary = "Atualiza o perfil da pessoa autenticada (username, nome e bio)")
    public UserResponseDTO atualizarMeuPerfil(@Valid @RequestBody UserUpdateRequestDTO dto) {
        return userService.atualizarPerfil(dto);
    }

    @GetMapping("/{id}/treinos")
    @Operation(summary = "Lista os treinos de um usuário")
    public List<TreinoResponseDTO> listarTreinos(@PathVariable Long id) {
        return treinoService.listarPorAutor(id);
    }
}
