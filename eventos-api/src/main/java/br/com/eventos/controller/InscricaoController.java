package br.com.eventos.controller;

import br.com.eventos.dto.ErroResponseDTO;
import br.com.eventos.dto.InscricaoRequestDTO;
import br.com.eventos.dto.InscricaoResponseDTO;
import br.com.eventos.model.StatusInscricao;
import br.com.eventos.service.InscricaoService;
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
@RequestMapping("/inscricoes")
@Tag(name = "Inscrições", description = "Inscrição de participantes em eventos (concentra as regras de negócio)")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado",
                content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Conflito com regra de negócio",
                content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
})
public class InscricaoController {

    private final InscricaoService service;

    public InscricaoController(InscricaoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Inscrever participante em um evento",
            description = "Valida evento aberto, não iniciado, sem duplicidade e com vagas. Estudante paga 50%.")
    @ApiResponse(responseCode = "201", description = "Inscrição realizada")
    public ResponseEntity<InscricaoResponseDTO> inscrever(@Valid @RequestBody InscricaoRequestDTO dto) {
        InscricaoResponseDTO criada = service.inscrever(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar inscrição por id")
    @ApiResponse(responseCode = "200", description = "Inscrição encontrada")
    public ResponseEntity<InscricaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar inscrições", description = "Filtros opcionais: eventoId, participanteId e status.")
    @ApiResponse(responseCode = "200", description = "Lista de inscrições")
    public ResponseEntity<List<InscricaoResponseDTO>> listar(
            @RequestParam(required = false) Long eventoId,
            @RequestParam(required = false) Long participanteId,
            @RequestParam(required = false) StatusInscricao status) {
        return ResponseEntity.ok(service.listar(eventoId, participanteId, status));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar inscrição", description = "Libera a vaga no evento.")
    @ApiResponse(responseCode = "200", description = "Inscrição cancelada")
    public ResponseEntity<InscricaoResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir inscrição", description = "Só é permitido para inscrições já canceladas.")
    @ApiResponse(responseCode = "204", description = "Inscrição excluída")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
