package br.com.alura.runnercircleapi.repository;

import br.com.alura.runnercircleapi.model.Treino;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TreinoRepository extends JpaRepository<Treino, Long> {

    @Query(
            value = "select t from Treino t left join fetch t.autor",
            countQuery = "select count(t) from Treino t"
    )
    Page<Treino> buscarTodosComAutor(Pageable pageable);

    @Query(
            value = """
                    select t from Treino t
                    left join fetch t.autor
                    where lower(t.descricao) like lower(:buscaPattern)
                    """,
            countQuery = """
                    select count(t) from Treino t
                    where lower(t.descricao) like lower(:buscaPattern)
                    """
    )
    Page<Treino> buscarPorDescricaoComAutor(@Param("buscaPattern") String buscaPattern, Pageable pageable);

    @EntityGraph(attributePaths = {"autor", "curtidas"})
    List<Treino> findByAutorId(Long autorId);
}
