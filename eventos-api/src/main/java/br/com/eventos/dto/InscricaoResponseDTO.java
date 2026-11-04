package br.com.eventos.dto;

import br.com.eventos.model.StatusInscricao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InscricaoResponseDTO(
        Long id,
        Long eventoId,
        String eventoNome,
        Long participanteId,
        String participanteNome,
        LocalDateTime dataInscricao,
        BigDecimal valorPago,
        StatusInscricao status) {
}
