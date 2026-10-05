package br.com.alura.runnercircleapi.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Pessoa autenticada, mas sem a role exigida (ex.: USER chamando endpoint de ADMIN).
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final RespostaErroJson respostaErroJson;

    public JwtAccessDeniedHandler(RespostaErroJson respostaErroJson) {
        this.respostaErroJson = respostaErroJson;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        respostaErroJson.escrever(request, response, HttpStatus.FORBIDDEN, "acesso negado");
    }
}
