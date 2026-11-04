package br.com.eventos.repository;

import br.com.eventos.model.Inscricao;
import br.com.eventos.model.StatusInscricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long>, JpaSpecificationExecutor<Inscricao> {

    long countByEventoIdAndStatus(Long eventoId, StatusInscricao status);

    boolean existsByEventoIdAndParticipanteIdAndStatus(Long eventoId, Long participanteId, StatusInscricao status);

    boolean existsByEventoId(Long eventoId);

    boolean existsByParticipanteId(Long participanteId);

    List<Inscricao> findByEventoIdAndStatus(Long eventoId, StatusInscricao status);
}
