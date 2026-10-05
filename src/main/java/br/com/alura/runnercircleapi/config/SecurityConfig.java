package br.com.alura.runnercircleapi.config;

import br.com.alura.runnercircleapi.security.JwtAccessDeniedHandler;
import br.com.alura.runnercircleapi.security.JwtAuthenticationEntryPoint;
import br.com.alura.runnercircleapi.security.JwtAuthenticationFilter;
import br.com.alura.runnercircleapi.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   JwtAuthenticationEntryPoint entryPoint,
                                                   JwtAccessDeniedHandler accessDeniedHandler) throws Exception {
        return http
                // A API autentica por token no header, sem sessão nem cookie: CSRF não se aplica.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/treinos").permitAll()
                        .requestMatchers("/treinos/**").authenticated()
                        .requestMatchers("/users/me").authenticated()
                        // TEMPORÁRIO: o resto (/users, /uploads, Swagger...) continua liberado por enquanto;
                        // GET /users é restrito por @PreAuthorize no controller.
                        .anyRequest().permitAll())
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
