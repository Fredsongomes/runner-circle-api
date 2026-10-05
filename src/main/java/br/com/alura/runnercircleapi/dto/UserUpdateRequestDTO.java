package br.com.alura.runnercircleapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDTO(

        @NotBlank(message = "o username é obrigatório")
        @Size(min = 3, max = 30, message = "o username deve ter entre 3 e 30 caracteres")
        String username,

        @NotBlank(message = "o nome é obrigatório")
        @Size(max = 100, message = "o nome deve ter no máximo 100 caracteres")
        String nome,

        // Opcional: null ou vazio remove a bio
        @Size(max = 255, message = "a bio deve ter no máximo 255 caracteres")
        String bio
) {
}
