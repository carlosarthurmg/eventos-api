package br.com.eventos.controller;

import br.com.eventos.dto.ErroResponseDTO;
import br.com.eventos.dto.EventoRequestDTO;
import br.com.eventos.dto.EventoResponseDTO;
import br.com.eventos.model.CategoriaEvento;
import br.com.eventos.model.StatusEvento;
import br.com.eventos.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/eventos")
@Tag(name = "Eventos", description = "Cadastro e gerenciamento de eventos")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado",
                content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Conflito com regra de negócio",
                content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
})
public class EventoController {

    private final EventoService service;

    public EventoController(EventoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastrar evento", description = "Cria um evento com status ABERTO.")
    @ApiResponse(responseCode = "201", description = "Evento criado")
    public ResponseEntity<EventoResponseDTO> criar(@Valid @RequestBody EventoRequestDTO dto) {
        EventoResponseDTO criado = service.criar(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento por id")
    @ApiResponse(responseCode = "200", description = "Evento encontrado")
    public ResponseEntity<EventoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar eventos com filtros e paginação",
            description = "Filtros opcionais: nome, categoria, status, intervalo de datas de início e intervalo de preço. "
                    + "Paginação/ordenação: page, size, sort (ex.: sort=dataInicio,desc).")
    @ApiResponse(responseCode = "200", description = "Lista paginada de eventos")
    public ResponseEntity<Page<EventoResponseDTO>> listar(
            @Parameter(description = "Trecho do nome (sem diferenciar maiúsculas)") @RequestParam(required = false) String nome,
            @RequestParam(required = false) CategoriaEvento categoria,
            @RequestParam(required = false) StatusEvento status,
            @Parameter(description = "Data de início a partir de (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDe,
            @Parameter(description = "Data de início até (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAte,
            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax,
            @ParameterObject @PageableDefault(size = 10, sort = "dataInicio") Pageable pageable) {
        return ResponseEntity.ok(service.listar(nome, categoria, status, dataDe, dataAte, precoMin, precoMax, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar evento")
    @ApiResponse(responseCode = "200", description = "Evento atualizado")
    public ResponseEntity<EventoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody EventoRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir evento", description = "Não permite excluir evento que possua inscrições.")
    @ApiResponse(responseCode = "204", description = "Evento excluído")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar evento", description = "Muda o status para CANCELADO e cancela as inscrições ativas.")
    @ApiResponse(responseCode = "200", description = "Evento cancelado")
    public ResponseEntity<EventoResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @PatchMapping("/{id}/encerrar")
    @Operation(summary = "Encerrar evento", description = "Muda o status para ENCERRADO.")
    @ApiResponse(responseCode = "200", description = "Evento encerrado")
    public ResponseEntity<EventoResponseDTO> encerrar(@PathVariable Long id) {
        return ResponseEntity.ok(service.encerrar(id));
    }
}
