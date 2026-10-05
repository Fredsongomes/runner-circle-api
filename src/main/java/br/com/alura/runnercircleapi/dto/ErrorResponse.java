package br.com.alura.runnercircleapi.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String mensagem,
        String caminho
) {
}
