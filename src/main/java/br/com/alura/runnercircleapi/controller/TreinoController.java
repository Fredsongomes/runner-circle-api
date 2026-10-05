package br.com.alura.runnercircleapi.controller;

import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.dto.TreinoResponseDTO;
import br.com.alura.runnercircleapi.service.TreinoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/treinos")
@Tag(name = "Treinos", description = "CRUD de treinos (corrida e caminhada) do Runner Circle")
public class TreinoController {

    @Autowired
    private TreinoService treinoService;

    @GetMapping
    @Operation(summary = "Lista os treinos paginados, do mais recente para o mais antigo, com busca opcional na descrição")
    public Page<TreinoResponseDTO> listar(
            @Parameter(description = "Trecho da descrição (sem diferenciar maiúsculas e minúsculas)")
            @RequestParam(required = false) String busca,
            @ParameterObject
            @PageableDefault(size = 10, sort = {"dataCriacao", "id"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        return treinoService.listar(busca, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um treino pelo id")
    public ResponseEntity<TreinoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(treinoService.buscarPorId(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cria um novo treino, com imagem opcional (jpg, jpeg, png ou webp, até 5 MB)")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            encoding = @Encoding(name = "dados", contentType = MediaType.APPLICATION_JSON_VALUE)
    ))
    public ResponseEntity<TreinoResponseDTO> criar(
            @Valid @RequestPart("dados") TreinoRequestDTO dados,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem) {
        return ResponseEntity.status(HttpStatus.CREATED).body(treinoService.criar(dados, imagem));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um treino existente")
    public ResponseEntity<TreinoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody TreinoRequestDTO dto) {
        return ResponseEntity.ok(treinoService.atualizar(id, dto));
    }

    @PostMapping("/{id}/curtir")
    @Operation(summary = "Curte um treino")
    public ResponseEntity<TreinoResponseDTO> curtir(@PathVariable Long id) {
        return ResponseEntity.ok(treinoService.curtir(id));
    }

    @DeleteMapping("/{id}/curtir")
    @Operation(summary = "Remove a curtida de um treino")
    public ResponseEntity<TreinoResponseDTO> descurtir(@PathVariable Long id) {
        return ResponseEntity.ok(treinoService.descurtir(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um treino")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        treinoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
