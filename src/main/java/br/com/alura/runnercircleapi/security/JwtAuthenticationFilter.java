package br.com.alura.runnercircleapi.security;

import br.com.alura.runnercircleapi.service.JwtService;
import br.com.alura.runnercircleapi.service.JwtService.UsuarioToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

// Não é @Component de propósito: o Spring Boot registraria o filtro também na cadeia de servlets,
// fora do Spring Security. Ele é criado e adicionado só no SecurityConfig.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Lido pelo JwtAuthenticationEntryPoint para diferenciar token ausente de token inválido.
    public static final String ATRIBUTO_TOKEN_INVALIDO = JwtAuthenticationFilter.class.getName() + ".TOKEN_INVALIDO";

    private static final String PREFIXO_BEARER = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.regionMatches(true, 0, PREFIXO_BEARER, 0, PREFIXO_BEARER.length())) {
            String token = header.substring(PREFIXO_BEARER.length()).trim();
            Optional<UsuarioToken> usuario = jwtService.validarToken(token);

            if (usuario.isPresent()) {
                List<SimpleGrantedAuthority> permissoes =
                        List.of(new SimpleGrantedAuthority("ROLE_" + usuario.get().role().name()));
                UsernamePasswordAuthenticationToken autenticacao =
                        UsernamePasswordAuthenticationToken.authenticated(usuario.get().userId(), null, permissoes);

                SecurityContext contexto = SecurityContextHolder.createEmptyContext();
                contexto.setAuthentication(autenticacao);
                SecurityContextHolder.setContext(contexto);
            } else {
                request.setAttribute(ATRIBUTO_TOKEN_INVALIDO, true);
            }
        }

        filterChain.doFilter(request, response);
    }
}
