package br.com.alura.runnercircleapi.dto;

import br.com.alura.runnercircleapi.model.Role;

public record UserAdminResponseDTO(
        Long id,
        String nome,
        String username,
        String email,
        Role role
) {
}
