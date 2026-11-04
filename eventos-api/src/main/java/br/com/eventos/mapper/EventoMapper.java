package br.com.eventos.mapper;

import br.com.eventos.dto.EventoRequestDTO;
import br.com.eventos.dto.EventoResponseDTO;
import br.com.eventos.model.Evento;

/** Conversão manual entre Evento e DTOs. */
public final class EventoMapper {

    private EventoMapper() {
    }

    public static Evento toEntity(EventoRequestDTO dto) {
        Evento e = new Evento();
        updateEntity(e, dto);
        return e;
    }

    public static void updateEntity(Evento e, EventoRequestDTO dto) {
        e.setNome(dto.nome().trim());
        e.setDescricao(dto.descricao());
        e.setLocal(dto.local().trim());
        e.setDataInicio(dto.dataInicio());
        e.setDataFim(dto.dataFim());
        e.setCapacidade(dto.capacidade());
        e.setPrecoIngresso(dto.precoIngresso());
        e.setCategoria(dto.categoria());
    }

    public static EventoResponseDTO toResponse(Evento e, long inscritosAtivos) {
        int vagas = Math.max(0, e.getCapacidade() - (int) inscritosAtivos);
        return new EventoResponseDTO(e.getId(), e.getNome(), e.getDescricao(), e.getLocal(),
                e.getDataInicio(), e.getDataFim(), e.getCapacidade(), vagas,
                e.getPrecoIngresso(), e.getCategoria(), e.getStatus());
    }
}
