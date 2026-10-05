package br.com.alura.runnercircleapi.mapper;

import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.dto.TreinoResponseDTO;
import br.com.alura.runnercircleapi.model.Treino;
import br.com.alura.runnercircleapi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TreinoMapper {

    @Autowired
    private UserMapper userMapper;

    public Treino toEntity(TreinoRequestDTO dto, User autor) {
        Treino treino = new Treino(
                dto.tipoTreino(),
                dto.tempoEmMinutos(),
                dto.distanciaMetros(),
                dto.calorias(),
                dto.batimentos(),
                dto.descricao()
        );
        treino.setAutor(autor);
        return treino;
    }

    public TreinoResponseDTO toResponseDTO(Treino treino) {
        return new TreinoResponseDTO(
                treino.getId(),
                treino.getTipoTreino(),
                treino.getTempoEmMinutos(),
                treino.getDistanciaMetros(),
                treino.getCalorias(),
                treino.getBatimentos(),
                treino.getDescricao(),
                treino.getImagemUrl(),
                treino.getDataCriacao(),
                userMapper.toAutorDTO(treino.getAutor()),
                treino.getCurtidas().size()
        );
    }

    public void atualizarEntidade(Treino treino, TreinoRequestDTO dto) {
        treino.setTipoTreino(dto.tipoTreino());
        treino.setTempoEmMinutos(dto.tempoEmMinutos());
        treino.setDistanciaMetros(dto.distanciaMetros());
        treino.setCalorias(dto.calorias());
        treino.setBatimentos(dto.batimentos());
        treino.setDescricao(dto.descricao());
    }
}
