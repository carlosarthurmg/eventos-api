package br.com.eventos.dto;

import br.com.eventos.model.CategoriaEvento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventoRequestDTO(

        @Schema(example = "Workshop de Spring Boot")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @Schema(example = "Introdução prática a APIs REST com Spring Boot")
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String descricao,

        @Schema(example = "Auditório Central")
        @NotBlank(message = "O local é obrigatório")
        @Size(max = 150, message = "O local deve ter no máximo 150 caracteres")
        String local,

        @Schema(example = "2027-03-20T19:00:00")
        @NotNull(message = "A data de início é obrigatória")
        LocalDateTime dataInicio,

        @Schema(example = "2027-03-20T22:00:00")
        @NotNull(message = "A data de fim é obrigatória")
        LocalDateTime dataFim,

        @Schema(example = "50")
        @NotNull(message = "A capacidade é obrigatória")
        @Min(value = 1, message = "A capacidade deve ser de pelo menos 1")
        Integer capacidade,

        @Schema(example = "80.00")
        @NotNull(message = "O preço do ingresso é obrigatório")
        @DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
        BigDecimal precoIngresso,

        @Schema(example = "WORKSHOP")
        @NotNull(message = "A categoria é obrigatória")
        CategoriaEvento categoria) {
}
