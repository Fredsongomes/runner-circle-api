package br.com.alura.runnercircleapi.mapper;

import br.com.alura.runnercircleapi.dto.ComentarioRequestDTO;
import br.com.alura.runnercircleapi.dto.ComentarioResponseDTO;
import br.com.alura.runnercircleapi.model.Comentario;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComentarioMapper {

    @Autowired
    private UserMapper userMapper;

    public Comentario toEntity(ComentarioRequestDTO dto, User autor, Treino treino) {
        return new Comentario(dto.texto(), autor, treino);
    }

    public ComentarioResponseDTO toResponseDTO(Comentario comentario) {
        return new ComentarioResponseDTO(
                comentario.getId(),
                comentario.getTexto(),
                comentario.getDataCriacao(),
                userMapper.toAutorDTO(comentario.getAutor())
        );
    }
}
