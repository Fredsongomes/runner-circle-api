package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.dto.TreinoResponseDTO;
import br.com.alura.runnercircleapi.exception.AcessoNegadoException;
import br.com.alura.runnercircleapi.exception.TreinoNotFoundException;
import br.com.alura.runnercircleapi.exception.UsuarioNaoEncontradoException;
import br.com.alura.runnercircleapi.mapper.TreinoMapper;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.TreinoRepository;
import br.com.alura.runnercircleapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TreinoService {

    private static final Logger log = LoggerFactory.getLogger(TreinoService.class);

    @Autowired
    private TreinoRepository treinoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TreinoMapper treinoMapper;

    @Autowired
    private ImagemStorageService imagemStorageService;

    @Autowired
    private UsuarioAutenticadoService usuarioAutenticadoService;

    public Page<TreinoResponseDTO> listar(String busca, Pageable pageable) {
        Page<Treino> treinos = StringUtils.hasText(busca)
                ? treinoRepository.buscarPorDescricaoComAutor("%" + busca.trim() + "%", pageable)
                : treinoRepository.buscarTodosComAutor(pageable);

        return treinos.map(treinoMapper::toResponseDTO);
    }

    public TreinoResponseDTO buscarPorId(Long id) {
        return treinoMapper.toResponseDTO(buscarTreino(id));
    }

    public List<TreinoResponseDTO> listarPorAutor(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UsuarioNaoEncontradoException(userId);
        }

        return treinoRepository.findByAutorId(userId).stream()
                .map(treinoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public TreinoResponseDTO criar(TreinoRequestDTO dto, MultipartFile imagem) {
        User autor = usuarioAutenticadoService.obterUsuarioAutenticado();

        Treino treino = treinoMapper.toEntity(dto, autor);

        if (imagem != null && !imagem.isEmpty()) {
            treino.setImagemUrl(imagemStorageService.salvar(imagem));
        }

        treino = treinoRepository.save(treino);
        log.info("Treino criado: id={}, autorId={}", treino.getId(), autor.getId());
        return treinoMapper.toResponseDTO(treino);
    }

    public TreinoResponseDTO atualizar(Long id, TreinoRequestDTO dto) {
        Treino treino = buscarTreino(id);
        exigirAutoria(treino, "só a pessoa autora pode editar este treino");

        treinoMapper.atualizarEntidade(treino, dto);
        treino = treinoRepository.save(treino);
        return treinoMapper.toResponseDTO(treino);
    }

    @Transactional
    public TreinoResponseDTO curtir(Long treinoId) {
        User usuario = usuarioAutenticadoService.obterUsuarioAutenticado();
        Treino treino = buscarTreino(treinoId);

        treino.getCurtidas().add(usuario);
        treino = treinoRepository.save(treino);
        return treinoMapper.toResponseDTO(treino);
    }

    @Transactional
    public TreinoResponseDTO descurtir(Long treinoId) {
        User usuario = usuarioAutenticadoService.obterUsuarioAutenticado();
        Treino treino = buscarTreino(treinoId);

        treino.getCurtidas().remove(usuario);
        treino = treinoRepository.save(treino);
        return treinoMapper.toResponseDTO(treino);
    }

    public void remover(Long id) {
        Treino treino = buscarTreino(id);
        exigirAutoria(treino, "só a pessoa autora pode remover este treino");

        treinoRepository.delete(treino);
    }

    private void exigirAutoria(Treino treino, String mensagem) {
        User autenticado = usuarioAutenticadoService.obterUsuarioAutenticado();
        if (!treino.getAutor().getId().equals(autenticado.getId())) {
            throw new AcessoNegadoException(mensagem);
        }
    }

    private Treino buscarTreino(Long id) {
        return treinoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tentativa de buscar treino inexistente: id={}", id);
                    return new TreinoNotFoundException(id);
                });
    }
}
