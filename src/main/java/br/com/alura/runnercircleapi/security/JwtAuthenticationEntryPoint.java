package br.com.alura.runnercircleapi.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final RespostaErroJson respostaErroJson;

    public JwtAuthenticationEntryPoint(RespostaErroJson respostaErroJson) {
        this.respostaErroJson = respostaErroJson;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String mensagem = request.getAttribute(JwtAuthenticationFilter.ATRIBUTO_TOKEN_INVALIDO) != null
                ? "token inválido ou expirado"
                : "autenticação necessária";

        respostaErroJson.escrever(request, response, HttpStatus.UNAUTHORIZED, mensagem);
    }
}
