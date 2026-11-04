package br.com.eventos.controller;

import br.com.eventos.dto.ErroResponseDTO;
import br.com.eventos.dto.ParticipanteRequestDTO;
import br.com.eventos.dto.ParticipanteResponseDTO;
import br.com.eventos.service.ParticipanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/participantes")
@Tag(name = "Participantes", description = "Cadastro de participantes")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado",
                content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Conflito com regra de negócio",
                content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
})
public class ParticipanteController {

    private final ParticipanteService service;

    public ParticipanteController(ParticipanteService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar participante", description = "O e-mail deve ser único.")
    @ApiResponse(responseCode = "201", description = "Participante criado")
    public ResponseEntity<ParticipanteResponseDTO> criar(@Valid @RequestBody ParticipanteRequestDTO dto) {
        ParticipanteResponseDTO criado = service.criar(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar participante por id")
    @ApiResponse(responseCode = "200", description = "Participante encontrado")
    public ResponseEntity<ParticipanteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar participantes", description = "Filtros opcionais: nome, email e estudante (true/false).")
    @ApiResponse(responseCode = "200", description = "Lista de participantes")
    public ResponseEntity<List<ParticipanteResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean estudante) {
        return ResponseEntity.ok(service.listar(nome, email, estudante));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar participante")
    @ApiResponse(responseCode = "200", description = "Participante atualizado")
    public ResponseEntity<ParticipanteResponseDTO> atualizar(@PathVariable Long id,
                                                             @Valid @RequestBody ParticipanteRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir participante", description = "Não permite excluir participante que possua inscrições.")
    @ApiResponse(responseCode = "204", description = "Participante excluído")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
