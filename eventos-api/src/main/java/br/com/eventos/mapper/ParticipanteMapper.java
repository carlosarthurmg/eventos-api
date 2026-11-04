package br.com.eventos.mapper;

import br.com.eventos.dto.ParticipanteRequestDTO;
import br.com.eventos.dto.ParticipanteResponseDTO;
import br.com.eventos.model.Participante;

/** Conversão manual entre Participante e DTOs. */
public final class ParticipanteMapper {

    private ParticipanteMapper() {
    }

    public static Participante toEntity(ParticipanteRequestDTO dto) {
        Participante p = new Participante();
        updateEntity(p, dto);
        return p;
    }

    public static void updateEntity(Participante p, ParticipanteRequestDTO dto) {
        p.setNome(dto.nome().trim());
        p.setEmail(dto.email().trim().toLowerCase());
        p.setTelefone(dto.telefone());
        p.setEstudante(dto.estudante());
    }

    public static ParticipanteResponseDTO toResponse(Participante p) {
        return new ParticipanteResponseDTO(p.getId(), p.getNome(), p.getEmail(), p.getTelefone(), p.getEstudante());
    }
}
