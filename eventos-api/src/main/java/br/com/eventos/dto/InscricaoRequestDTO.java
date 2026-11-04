package br.com.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record InscricaoRequestDTO(

        @Schema(example = "1")
        @NotNull(message = "O id do evento é obrigatório")
        Long eventoId,

        @Schema(example = "1")
        @NotNull(message = "O id do participante é obrigatório")
        Long participanteId) {
}
