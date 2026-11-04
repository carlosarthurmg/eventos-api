package br.com.eventos.mapper;

import br.com.eventos.dto.InscricaoResponseDTO;
import br.com.eventos.model.Inscricao;

/** Conversão manual de Inscricao para DTO. */
public final class InscricaoMapper {

    private InscricaoMapper() {
    }

    public static InscricaoResponseDTO toResponse(Inscricao i) {
        return new InscricaoResponseDTO(i.getId(),
                i.getEvento().getId(), i.getEvento().getNome(),
                i.getParticipante().getId(), i.getParticipante().getNome(),
                i.getDataInscricao(), i.getValorPago(), i.getStatus());
    }
}
