package br.com.alura.runnercircleapi.dto;

import br.com.alura.runnercircleapi.model.Role;

public record LoginResponseDTO(
        String token,
        Role role,
        UserResponseDTO usuario
) {
}
