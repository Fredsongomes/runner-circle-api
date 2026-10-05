package br.com.alura.runnercircleapi.security;

import br.com.alura.runnercircleapi.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

// Escreve o ErrorResponse direto na resposta, para os erros que o Spring Security trata
// antes de a requisição chegar ao GlobalExceptionHandler.
@Component
public class RespostaErroJson {

    private static final Logger log = LoggerFactory.getLogger(RespostaErroJson.class);

    private final ObjectMapper objectMapper;

    public RespostaErroJson(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void escrever(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
                         String mensagem) throws IOException {
        log.warn("{} {} -> {}: {}", request.getMethod(), request.getRequestURI(), status.value(), mensagem);

        ErrorResponse corpo = new ErrorResponse(LocalDateTime.now(), status.value(), mensagem, request.getRequestURI());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getOutputStream().write(objectMapper.writeValueAsBytes(corpo));
    }
}
