package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.dto.UserAdminResponseDTO;
import br.com.alura.runnercircleapi.dto.UserResponseDTO;
import br.com.alura.runnercircleapi.dto.UserUpdateRequestDTO;
import br.com.alura.runnercircleapi.exception.UsuarioJaExisteException;
import br.com.alura.runnercircleapi.mapper.UserMapper;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UsuarioAutenticadoService usuarioAutenticadoService;

    public List<UserAdminResponseDTO> listarTodos() {
        return userRepository.findAll(Sort.by("id")).stream()
                .map(userMapper::toAdminResponseDTO)
                .collect(Collectors.toList());
    }

    // Atualiza sempre a pessoa autenticada: o id vem do token, nunca da requisição.
    public UserResponseDTO atualizarPerfil(UserUpdateRequestDTO dto) {
        User user = usuarioAutenticadoService.obterUsuarioAutenticado();
        String username = dto.username().trim();

        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(username, user.getId())) {
            throw new UsuarioJaExisteException("username já está em uso");
        }

        user.setUsername(username);
        user.setNome(dto.nome().trim());
        user.setBio(StringUtils.hasText(dto.bio()) ? dto.bio().trim() : null);

        user = userRepository.save(user);
        return userMapper.toResponseDTO(user);
    }
}
