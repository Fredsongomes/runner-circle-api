package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.dto.TreinoResponseDTO;
import br.com.alura.runnercircleapi.exception.AcessoNegadoException;
import br.com.alura.runnercircleapi.exception.TreinoNotFoundException;
import br.com.alura.runnercircleapi.mapper.TreinoMapper;
import br.com.alura.runnercircleapi.model.TipoTreino;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.TreinoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreinoServiceTest {

    private static final Long ID_AUTOR = 10L;
    private static final Long ID_OUTRA_PESSOA = 20L;

    @Mock
    private TreinoRepository treinoRepository;

    // buscarPorId e atualizar convertem a entidade com o TreinoMapper; sem este mock o campo ficaria null.
    @Mock
    private TreinoMapper treinoMapper;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private TreinoService treinoService;

    @Test
    void buscarTreinoId_quandoExiste_retornaTreino() {
        // Arrange
        Long id = 1L;
        Treino treino = new Treino(TipoTreino.CORRIDA, 30, 5000, 300, 140, "corrida no parque");
        treino.setId(id);

        TreinoResponseDTO esperado = new TreinoResponseDTO(id, TipoTreino.CORRIDA, 30, 5000, 300, 140,
                "corrida no parque", null, null, null, 0);

        when(treinoRepository.findById(id)).thenReturn(Optional.of(treino));
        when(treinoMapper.toResponseDTO(treino)).thenReturn(esperado);

        // Act
        TreinoResponseDTO resultado = treinoService.buscarPorId(id);

        // Assert
        assertThat(resultado).isEqualTo(esperado);
        assertThat(resultado.id()).isEqualTo(id);
        assertThat(resultado.descricao()).isEqualTo("corrida no parque");
        verify(treinoRepository).findById(id);
        verify(treinoMapper).toResponseDTO(treino);
    }

    @Test
    void buscarTreinoPorId_quandoNaoExiste_lancaTreinoNotFoundException() {
        // Arrange
        Long id = 99L;
        when(treinoRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        TreinoNotFoundException exception = assertThrows(TreinoNotFoundException.class,
                () -> treinoService.buscarPorId(id));

        // Assert
        assertThat(exception.getMessage()).isEqualTo("treino 99 não encontrado");
        verifyNoInteractions(treinoMapper);
    }

    @Test
    void atualizarTreino_quandoNaoEhAutor_lancaAcessoNegadoException() {
        // Arrange
        Treino treino = treinoDoAutor(1L, usuario(ID_AUTOR));
        TreinoRequestDTO dto = requestDeExemplo();

        when(treinoRepository.findById(1L)).thenReturn(Optional.of(treino));
        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuario(ID_OUTRA_PESSOA));

        // Act
        AcessoNegadoException exception = assertThrows(AcessoNegadoException.class,
                () -> treinoService.atualizar(1L, dto));

        // Assert
        assertThat(exception.getMessage()).isEqualTo("só a pessoa autora pode editar este treino");
        verify(treinoMapper, never()).atualizarEntidade(any(), any());
        verify(treinoRepository, never()).save(any());
    }

    @Test
    void atualizarTreino_quandoEhAutor_atualizaComSucesso() {
        // Arrange
        User autor = usuario(ID_AUTOR);
        Treino treino = treinoDoAutor(1L, autor);
        TreinoRequestDTO dto = requestDeExemplo();
        TreinoResponseDTO esperado = new TreinoResponseDTO(1L, TipoTreino.CAMINHADA, 45, 4000, 200, 110,
                "caminhada atualizada", null, null, null, 0);

        when(treinoRepository.findById(1L)).thenReturn(Optional.of(treino));
        when(usuarioAutenticadoService.obterUsuarioAutenticado()).thenReturn(usuario(ID_AUTOR));
        when(treinoRepository.save(treino)).thenReturn(treino);
        when(treinoMapper.toResponseDTO(treino)).thenReturn(esperado);

        // Act
        TreinoResponseDTO resultado = treinoService.atualizar(1L, dto);

        // Assert
        assertThat(resultado).isEqualTo(esperado);
        InOrder ordem = inOrder(treinoMapper, treinoRepository);
        ordem.verify(treinoMapper).atualizarEntidade(treino, dto);
        ordem.verify(treinoRepository).save(treino);
        ordem.verify(treinoMapper).toResponseDTO(treino);
    }

    private static User usuario(Long id) {
        User user = new User("Pessoa " + id, "pessoa" + id, "pessoa" + id + "@exemplo.com", "hash");
        user.setId(id);
        return user;
    }

    private static Treino treinoDoAutor(Long id, User autor) {
        Treino treino = new Treino(TipoTreino.CORRIDA, 30, 5000, 300, 140, "corrida no parque");
        treino.setId(id);
        treino.setAutor(autor);
        return treino;
    }

    private static TreinoRequestDTO requestDeExemplo() {
        return new TreinoRequestDTO(TipoTreino.CAMINHADA, 45, 4000, 200, 110, "caminhada atualizada");
    }
}
