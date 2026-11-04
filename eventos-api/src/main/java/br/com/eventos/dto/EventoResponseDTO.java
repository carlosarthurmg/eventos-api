package br.com.eventos.dto;

import br.com.eventos.model.CategoriaEvento;
import br.com.eventos.model.StatusEvento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventoResponseDTO(
        Long id,
        String nome,
        String descricao,
        String local,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        Integer capacidade,
        Integer vagasDisponiveis,
        BigDecimal precoIngresso,
        CategoriaEvento categoria,
        StatusEvento status) {
}
