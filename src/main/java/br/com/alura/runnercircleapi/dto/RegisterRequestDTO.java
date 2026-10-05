package br.com.alura.runnercircleapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

        @NotBlank(message = "o nome é obrigatório")
        @Size(max = 100, message = "o nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "o username é obrigatório")
        @Size(min = 3, max = 30, message = "o username deve ter entre 3 e 30 caracteres")
        String username,

        @NotBlank(message = "o email é obrigatório")
        @Email(message = "o email deve ser válido")
        String email,

        // BCrypt considera só os primeiros 72 bytes da senha
        @NotBlank(message = "a senha é obrigatória")
        @Size(min = 8, max = 72, message = "a senha deve ter entre 8 e 72 caracteres")
        String senha
) {
}
