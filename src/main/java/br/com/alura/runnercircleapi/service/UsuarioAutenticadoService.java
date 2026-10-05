package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAutenticadoService {

    @Autowired
    private UserRepository userRepository;

    // O JwtAuthenticationFilter guarda o id do usuário (Long) como principal.
    // Sem autenticação, ou com um token de usuário que não existe mais, a exceção do Spring Security
    // vira 401 no JwtAuthenticationEntryPoint.
    public User obterUsuarioAutenticado() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof Long userId)) {
            throw new AuthenticationCredentialsNotFoundException("nenhum usuário autenticado");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("usuário do token não existe"));
    }
}
