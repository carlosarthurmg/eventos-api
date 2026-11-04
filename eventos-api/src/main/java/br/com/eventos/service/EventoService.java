package br.com.eventos.service;

import br.com.eventos.dto.EventoRequestDTO;
import br.com.eventos.dto.EventoResponseDTO;
import br.com.eventos.exception.RecursoNaoEncontradoException;
import br.com.eventos.exception.RegraNegocioException;
import br.com.eventos.mapper.EventoMapper;
import br.com.eventos.model.CategoriaEvento;
import br.com.eventos.model.Evento;
import br.com.eventos.model.Inscricao;
import br.com.eventos.model.StatusEvento;
import br.com.eventos.model.StatusInscricao;
import br.com.eventos.repository.EventoRepository;
import br.com.eventos.repository.InscricaoRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventoService {

    private static final Logger log = LoggerFactory.getLogger(EventoService.class);

    private final EventoRepository eventoRepository;
    private final InscricaoRepository inscricaoRepository;

    public EventoService(EventoRepository eventoRepository, InscricaoRepository inscricaoRepository) {
        this.eventoRepository = eventoRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Transactional
    public EventoResponseDTO criar(EventoRequestDTO dto) {
        validarDatas(dto);
        // REGRA: evento deve ser criado com data de início futura
        if (dto.dataInicio().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("A data de início do evento deve ser futura.");
        }
        // REGRA: não permitir evento duplicado (mesmo nome e mesma data/hora de início)
        if (eventoRepository.existsByNomeIgnoreCaseAndDataInicio(dto.nome().trim(), dto.dataInicio())) {
            throw new RegraNegocioException("Já existe um evento com este nome nesta data/hora.");
        }
        Evento evento = EventoMapper.toEntity(dto);
        evento.setStatus(StatusEvento.ABERTO);
        evento = eventoRepository.save(evento);
        log.info("Evento criado: id={}, nome={}", evento.getId(), evento.getNome());
        return toResponse(evento);
    }

    @Transactional(readOnly = true)
    public EventoResponseDTO buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<EventoResponseDTO> listar(String nome, CategoriaEvento categoria, StatusEvento status,
                                          LocalDate dataDe, LocalDate dataAte,
                                          BigDecimal precoMin, BigDecimal precoMax, Pageable pageable) {
        Specification<Evento> spec = filtrar(nome, categoria, status, dataDe, dataAte, precoMin, precoMax);
        return eventoRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public EventoResponseDTO atualizar(Long id, EventoRequestDTO dto) {
        Evento evento = buscarEntidade(id);
        // REGRA: só é possível editar eventos com status ABERTO
        if (evento.getStatus() != StatusEvento.ABERTO) {
            throw new RegraNegocioException("Não é possível editar um evento com status " + evento.getStatus() + ".");
        }
        validarDatas(dto);
        // REGRA: capacidade não pode ficar abaixo do número de inscritos ativos
        long inscritos = inscricaoRepository.countByEventoIdAndStatus(id, StatusInscricao.ATIVA);
        if (dto.capacidade() < inscritos) {
            throw new RegraNegocioException("A capacidade (" + dto.capacidade()
                    + ") não pode ser menor que o número de inscritos ativos (" + inscritos + ").");
        }
        if (eventoRepository.existsByNomeIgnoreCaseAndDataInicioAndIdNot(dto.nome().trim(), dto.dataInicio(), id)) {
            throw new RegraNegocioException("Já existe outro evento com este nome nesta data/hora.");
        }
        EventoMapper.updateEntity(evento, dto);
        log.info("Evento atualizado: id={}", id);
        return toResponse(eventoRepository.save(evento));
    }

    @Transactional
    public void excluir(Long id) {
        Evento evento = buscarEntidade(id);
        // REGRA: não excluir evento que possua inscrições
        if (inscricaoRepository.existsByEventoId(id)) {
            throw new RegraNegocioException("Não é possível excluir um evento que possui inscrições. Cancele o evento em vez de excluí-lo.");
        }
        eventoRepository.delete(evento);
        log.info("Evento excluído: id={}", id);
    }

    @Transactional
    public EventoResponseDTO cancelar(Long id) {
        Evento evento = buscarEntidade(id);
        if (evento.getStatus() != StatusEvento.ABERTO) {
            throw new RegraNegocioException("Apenas eventos ABERTOS podem ser cancelados (status atual: " + evento.getStatus() + ").");
        }
        evento.setStatus(StatusEvento.CANCELADO);
        // REGRA: ao cancelar o evento, todas as inscrições ativas são canceladas
        List<Inscricao> ativas = inscricaoRepository.findByEventoIdAndStatus(id, StatusInscricao.ATIVA);
        ativas.forEach(i -> i.setStatus(StatusInscricao.CANCELADA));
        log.info("Evento cancelado: id={}, inscrições canceladas={}", id, ativas.size());
        return toResponse(eventoRepository.save(evento));
    }

    @Transactional
    public EventoResponseDTO encerrar(Long id) {
        Evento evento = buscarEntidade(id);
        if (evento.getStatus() != StatusEvento.ABERTO) {
            throw new RegraNegocioException("Apenas eventos ABERTOS podem ser encerrados (status atual: " + evento.getStatus() + ").");
        }
        evento.setStatus(StatusEvento.ENCERRADO);
        log.info("Evento encerrado: id={}", id);
        return toResponse(eventoRepository.save(evento));
    }

    // ---------- auxiliares ----------

    private Evento buscarEntidade(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado com id " + id));
    }

    private EventoResponseDTO toResponse(Evento e) {
        long inscritos = inscricaoRepository.countByEventoIdAndStatus(e.getId(), StatusInscricao.ATIVA);
        return EventoMapper.toResponse(e, inscritos);
    }

    // REGRA: data de fim não pode ser anterior à data de início
    private void validarDatas(EventoRequestDTO dto) {
        if (dto.dataFim().isBefore(dto.dataInicio())) {
            throw new RegraNegocioException("A data de fim não pode ser anterior à data de início.");
        }
    }

    private Specification<Evento> filtrar(String nome, CategoriaEvento categoria, StatusEvento status,
                                          LocalDate dataDe, LocalDate dataAte,
                                          BigDecimal precoMin, BigDecimal precoMax) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (nome != null && !nome.isBlank()) {
                p.add(cb.like(cb.lower(root.<String>get("nome")), "%" + nome.trim().toLowerCase() + "%"));
            }
            if (categoria != null) {
                p.add(cb.equal(root.get("categoria"), categoria));
            }
            if (status != null) {
                p.add(cb.equal(root.get("status"), status));
            }
            if (dataDe != null) {
                p.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("dataInicio"), dataDe.atStartOfDay()));
            }
            if (dataAte != null) {
                p.add(cb.lessThan(root.<LocalDateTime>get("dataInicio"), dataAte.plusDays(1).atStartOfDay()));
            }
            if (precoMin != null) {
                p.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("precoIngresso"), precoMin));
            }
            if (precoMax != null) {
                p.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("precoIngresso"), precoMax));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
