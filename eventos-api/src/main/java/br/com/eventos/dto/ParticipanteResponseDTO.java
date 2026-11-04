package br.com.eventos.dto;

public record ParticipanteResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        Boolean estudante) {
}
