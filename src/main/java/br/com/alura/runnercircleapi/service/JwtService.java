package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.model.Role;
import br.com.alura.runnercircleapi.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private static final String EMISSOR = "runner-circle-api";
    private static final int TAMANHO_MINIMO_CHAVE_BYTES = 32;
    private static final String CLAIM_ROLE = "role";

    private final SecretKey chave;
    private final Duration expiracao;
    private final Duration expiracaoLembrarMe;

    public JwtService(@Value("${app.jwt.secret}") String segredo,
                      @Value("${app.jwt.expiracao}") Duration expiracao,
                      @Value("${app.jwt.expiracao-lembrar-me}") Duration expiracaoLembrarMe) {
        byte[] bytesDaChave = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytesDaChave.length < TAMANHO_MINIMO_CHAVE_BYTES) {
            throw new IllegalStateException("JWT_SECRET deve ter pelo menos " + TAMANHO_MINIMO_CHAVE_BYTES + " bytes");
        }
        this.chave = Keys.hmacShaKeyFor(bytesDaChave);
        this.expiracao = expiracao;
        this.expiracaoLembrarMe = expiracaoLembrarMe;
    }

    public String gerarToken(User user, boolean lembrarMe) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(lembrarMe ? expiracaoLembrarMe : expiracao);

        return Jwts.builder()
                .issuer(EMISSOR)
                .subject(user.getId().toString())
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiraEm))
                .signWith(chave)
                .compact();
    }

    // Devolve id e role quando o token tem assinatura válida, é deste emissor, não expirou e traz uma role conhecida.
    // Token sem role (gerado antes da claim existir) é tratado como inválido: a pessoa precisa fazer login de novo.
    public Optional<UsuarioToken> validarToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(chave)
                    .requireIssuer(EMISSOR)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String role = claims.get(CLAIM_ROLE, String.class);
            if (role == null) {
                return Optional.empty();
            }
            return Optional.of(new UsuarioToken(Long.valueOf(claims.getSubject()), Role.valueOf(role)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public record UsuarioToken(Long userId, Role role) {
    }
}
