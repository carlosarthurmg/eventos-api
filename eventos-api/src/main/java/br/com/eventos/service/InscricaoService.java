package br.com.eventos.service;

import br.com.eventos.dto.InscricaoRequestDTO;
import br.com.eventos.dto.InscricaoResponseDTO;
import br.com.eventos.exception.RecursoNaoEncontradoException;
import br.com.eventos.exception.RegraNegocioException;
import br.com.eventos.mapper.InscricaoMapper;
import br.com.eventos.model.Evento;
import br.com.eventos.model.Inscricao;
import br.com.eventos.model.Participante;
import br.com.eventos.model.StatusEvento;
import br.com.eventos.model.StatusInscricao;
import br.com.eventos.repository.EventoRepository;
import br.com.eventos.repository.InscricaoRepository;
import br.com.eventos.repository.ParticipanteRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InscricaoService {

    private static final Logger log = LoggerFactory.getLogger(InscricaoService.class);
    private static final BigDecimal DESCONTO_ESTUDANTE = new BigDecimal("0.50");

    private final InscricaoRepository inscricaoRepository;
    private final EventoRepository eventoRepository;
    private final ParticipanteRepository participanteRepository;

    public InscricaoService(InscricaoRepository inscricaoRepository, EventoRepository eventoRepository,
                            ParticipanteRepository participanteRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.eventoRepository = eventoRepository;
        this.participanteRepository = participanteRepository;
    }

    @Transactional
    public InscricaoResponseDTO inscrever(InscricaoRequestDTO dto) {
        Evento evento = eventoRepository.findById(dto.eventoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado com id " + dto.eventoId()));
        Participante participante = participanteRepository.findById(dto.participanteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Participante não encontrado com id " + dto.participanteId()));

        // REGRA: só é possível se inscrever em evento ABERTO
        if (evento.getStatus() != StatusEvento.ABERTO) {
            throw new RegraNegocioException("O evento não está aberto para inscrições (status: " + evento.getStatus() + ").");
        }
        // REGRA: não é possível se inscrever em evento que já começou
        if (!evento.getDataInicio().isAfter(LocalDateTime.now())) {
            throw new RegraNegocioException("O evento já começou; inscrições encerradas.");
        }
        // REGRA: não permitir inscrição duplicada do mesmo participante no mesmo evento
        if (inscricaoRepository.existsByEventoIdAndParticipanteIdAndStatus(
                evento.getId(), participante.getId(), StatusInscricao.ATIVA)) {
            throw new RegraNegocioException("O participante já está inscrito neste evento.");
        }
        // REGRA: respeitar a capacidade do evento
        long ativas = inscricaoRepository.countByEventoIdAndStatus(evento.getId(), StatusInscricao.ATIVA);
        if (ativas >= evento.getCapacidade()) {
            throw new RegraNegocioException("O evento está lotado (capacidade: " + evento.getCapacidade() + ").");
        }

        // REGRA (valor calculado): estudantes pagam 50% do ingresso
        BigDecimal valor = evento.getPrecoIngresso();
        if (Boolean.TRUE.equals(participante.getEstudante())) {
            valor = valor.multiply(DESCONTO_ESTUDANTE).setScale(2, RoundingMode.HALF_UP);
        }

        Inscricao inscricao = new Inscricao();
        inscricao.setEvento(evento);
        inscricao.setParticipante(participante);
        inscricao.setDataInscricao(LocalDateTime.now());
        inscricao.setValorPago(valor);
        inscricao.setStatus(StatusInscricao.ATIVA);
        inscricao = inscricaoRepository.save(inscricao);
        log.info("Inscrição criada: id={}, evento={}, participante={}, valor={}",
                inscricao.getId(), evento.getId(), participante.getId(), valor);
        return InscricaoMapper.toResponse(inscricao);
    }

    @Transactional(readOnly = true)
    public InscricaoResponseDTO buscarPorId(Long id) {
        return InscricaoMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<InscricaoResponseDTO> listar(Long eventoId, Long participanteId, StatusInscricao status) {
        Specification<Inscricao> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (eventoId != null) {
                p.add(cb.equal(root.get("evento").get("id"), eventoId));
            }
            if (participanteId != null) {
                p.add(cb.equal(root.get("participante").get("id"), participanteId));
            }
            if (status != null) {
                p.add(cb.equal(root.get("status"), status));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        return inscricaoRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "dataInscricao")).stream()
                .map(InscricaoMapper::toResponse).toList();
    }

    @Transactional
    public InscricaoResponseDTO cancelar(Long id) {
        Inscricao inscricao = buscarEntidade(id);
        if (inscricao.getStatus() == StatusInscricao.CANCELADA) {
            throw new RegraNegocioException("A inscrição já está cancelada.");
        }
        inscricao.setStatus(StatusInscricao.CANCELADA);
        log.info("Inscrição cancelada: id={}", id);
        return InscricaoMapper.toResponse(inscricaoRepository.save(inscricao));
    }

    @Transactional
    public void excluir(Long id) {
        Inscricao inscricao = buscarEntidade(id);
        // REGRA: só é possível excluir inscrições já canceladas
        if (inscricao.getStatus() != StatusInscricao.CANCELADA) {
            throw new RegraNegocioException("Só é possível excluir inscrições canceladas. Cancele a inscrição primeiro.");
        }
        inscricaoRepository.delete(inscricao);
        log.info("Inscrição excluída: id={}", id);
    }

    private Inscricao buscarEntidade(Long id) {
        return inscricaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Inscrição não encontrada com id " + id));
    }
}
