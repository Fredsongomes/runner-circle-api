package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.dto.TreinoResponseDTO;
import br.com.alura.runnercircleapi.model.TipoTreino;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.TreinoRepository;
import br.com.alura.runnercircleapi.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TreinoIntegrationTest {

    @Autowired
    private TreinoService treinoService;

    @Autowired
    private TreinoRepository treinoRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void criarTreino_comAutorExistente_persisteDadosEnviados() {
        // Arrange
        User autor = criarPessoaAutenticada();
        TreinoRequestDTO dto = new TreinoRequestDTO(TipoTreino.CORRIDA, 42, 8000, 520, 155, "longão de domingo");

        // Act
        TreinoResponseDTO criado = treinoService.criar(dto, null);

        // Assert: relê do banco, sem passar pelo service
        Treino persistido = treinoRepository.findById(criado.id()).orElseThrow();

        assertThat(persistido.getTipoTreino()).isEqualTo(TipoTreino.CORRIDA);
        assertThat(persistido.getTempoEmMinutos()).isEqualTo(42);
        assertThat(persistido.getDistanciaMetros()).isEqualTo(8000);
        assertThat(persistido.getCalorias()).isEqualTo(520);
        assertThat(persistido.getBatimentos()).isEqualTo(155);
        assertThat(persistido.getDescricao()).isEqualTo("longão de domingo");
        assertThat(persistido.getImagemUrl()).isNull();
        assertThat(persistido.getDataCriacao()).isNotNull();
        assertThat(persistido.getAutor().getId()).isEqualTo(autor.getId());
    }

    @Test
    void criarTreino_comDescricaoNoLimiteDe500Caracteres_persisteSemErro() {
        // Arrange: o DTO aceita até 500 caracteres, então a coluna precisa aceitar o mesmo
        criarPessoaAutenticada();
        String descricao = "a".repeat(500);
        TreinoRequestDTO dto = new TreinoRequestDTO(TipoTreino.CAMINHADA, 60, 6000, 350, 120, descricao);

        // Act
        TreinoResponseDTO criado = treinoService.criar(dto, null);

        // Assert
        Treino persistido = treinoRepository.findById(criado.id()).orElseThrow();
        assertThat(persistido.getDescricao()).hasSize(500);
    }

    // O TreinoService usa a pessoa autenticada como autora; aqui ela é colocada direto no SecurityContext,
    // do mesmo jeito que o JwtAuthenticationFilter faz (principal = id do usuário).
    private User criarPessoaAutenticada() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        User pessoa = userRepository.save(new User("Pessoa Teste", "pessoa_" + sufixo, sufixo + "@teste.com", "hash"));

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(pessoa.getId(), null, List.of()));
        return pessoa;
    }
}
