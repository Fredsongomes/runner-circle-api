package br.com.alura.runnercircleapi.controller;

import br.com.alura.runnercircleapi.dto.ComentarioRequestDTO;
import br.com.alura.runnercircleapi.dto.ComentarioResponseDTO;
import br.com.alura.runnercircleapi.service.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/treinos/{id}/comentarios")
@Tag(name = "Comentários", description = "Comentários dos treinos do Runner Circle")
public class ComentarioController {

    @Autowired
    private ComentarioService comentarioService;

    @GetMapping
    @Operation(summary = "Lista os comentários de um treino, do mais antigo para o mais recente")
    public ResponseEntity<List<ComentarioResponseDTO>> listar(@PathVariable Long id) {
        return ResponseEntity.ok(comentarioService.listarPorTreino(id));
    }

    @PostMapping
    @Operation(summary = "Comenta em um treino")
    public ResponseEntity<ComentarioResponseDTO> criar(@PathVariable Long id, @Valid @RequestBody ComentarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comentarioService.criar(id, dto));
    }

    @DeleteMapping("/{comentarioId}")
    @Operation(summary = "Remove um comentário (pessoa autora do comentário ou do treino)")
    public ResponseEntity<Void> remover(@PathVariable Long id, @PathVariable Long comentarioId) {
        comentarioService.remover(id, comentarioId);
        return ResponseEntity.noContent().build();
    }
}
