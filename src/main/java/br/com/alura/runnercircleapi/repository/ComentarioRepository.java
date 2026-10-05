package br.com.alura.runnercircleapi.repository;

import br.com.alura.runnercircleapi.model.Comentario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    @EntityGraph(attributePaths = "autor")
    List<Comentario> findByTreinoIdOrderByDataCriacaoAsc(Long treinoId);

    Optional<Comentario> findByIdAndTreinoId(Long id, Long treinoId);
}
