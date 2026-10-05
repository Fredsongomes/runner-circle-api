package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.dto.ComentarioRequestDTO;
import br.com.alura.runnercircleapi.dto.ComentarioResponseDTO;
import br.com.alura.runnercircleapi.exception.AcessoNegadoException;
import br.com.alura.runnercircleapi.exception.ComentarioNotFoundException;
import br.com.alura.runnercircleapi.exception.TreinoNotFoundException;
import br.com.alura.runnercircleapi.mapper.ComentarioMapper;
import br.com.alura.runnercircleapi.model.Comentario;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.ComentarioRepository;
import br.com.alura.runnercircleapi.repository.TreinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComentarioService {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private TreinoRepository treinoRepository;

    @Autowired
    private ComentarioMapper comentarioMapper;

    @Autowired
    private UsuarioAutenticadoService usuarioAutenticadoService;

    public List<ComentarioResponseDTO> listarPorTreino(Long treinoId) {
        if (!treinoRepository.existsById(treinoId)) {
            throw new TreinoNotFoundException(treinoId);
        }

        return comentarioRepository.findByTreinoIdOrderByDataCriacaoAsc(treinoId).stream()
                .map(comentarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ComentarioResponseDTO criar(Long treinoId, ComentarioRequestDTO dto) {
        Treino treino = treinoRepository.findById(treinoId)
                .orElseThrow(() -> new TreinoNotFoundException(treinoId));
        User autor = usuarioAutenticadoService.obterUsuarioAutenticado();

        Comentario comentario = comentarioMapper.toEntity(dto, autor, treino);
        comentario = comentarioRepository.save(comentario);
        return comentarioMapper.toResponseDTO(comentario);
    }

    // Pode remover: a pessoa autora do comentário ou a pessoa autora do treino comentado.
    public void remover(Long treinoId, Long comentarioId) {
        if (!treinoRepository.existsById(treinoId)) {
            throw new TreinoNotFoundException(treinoId);
        }
        Comentario comentario = comentarioRepository.findByIdAndTreinoId(comentarioId, treinoId)
                .orElseThrow(() -> new ComentarioNotFoundException(comentarioId));

        Long autenticadoId = usuarioAutenticadoService.obterUsuarioAutenticado().getId();
        boolean autorDoComentario = comentario.getAutor().getId().equals(autenticadoId);
        boolean autorDoTreino = comentario.getTreino().getAutor().getId().equals(autenticadoId);

        if (!autorDoComentario && !autorDoTreino) {
            throw new AcessoNegadoException("só a pessoa autora do comentário ou do treino pode removê-lo");
        }

        comentarioRepository.delete(comentario);
    }
}
