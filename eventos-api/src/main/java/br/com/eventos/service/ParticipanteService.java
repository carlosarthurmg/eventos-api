package br.com.eventos.service;

import br.com.eventos.dto.ParticipanteRequestDTO;
import br.com.eventos.dto.ParticipanteResponseDTO;
import br.com.eventos.exception.RecursoNaoEncontradoException;
import br.com.eventos.exception.RegraNegocioException;
import br.com.eventos.mapper.ParticipanteMapper;
import br.com.eventos.model.Participante;
import br.com.eventos.repository.InscricaoRepository;
import br.com.eventos.repository.ParticipanteRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParticipanteService {

    private static final Logger log = LoggerFactory.getLogger(ParticipanteService.class);

    private final ParticipanteRepository participanteRepository;
    private final InscricaoRepository inscricaoRepository;

    public ParticipanteService(ParticipanteRepository participanteRepository, InscricaoRepository inscricaoRepository) {
        this.participanteRepository = participanteRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Transactional
    public ParticipanteResponseDTO criar(ParticipanteRequestDTO dto) {
        // REGRA: e-mail deve ser único
        if (participanteRepository.existsByEmailIgnoreCase(dto.email().trim())) {
            throw new RegraNegocioException("Já existe um participante cadastrado com o e-mail " + dto.email().trim() + ".");
        }
        Participante p = participanteRepository.save(ParticipanteMapper.toEntity(dto));
        log.info("Participante criado: id={}", p.getId());
        return ParticipanteMapper.toResponse(p);
    }

    @Transactional(readOnly = true)
    public ParticipanteResponseDTO buscarPorId(Long id) {
        return ParticipanteMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<ParticipanteResponseDTO> listar(String nome, String email, Boolean estudante) {
        Specification<Participante> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (nome != null && !nome.isBlank()) {
                p.add(cb.like(cb.lower(root.<String>get("nome")), "%" + nome.trim().toLowerCase() + "%"));
            }
            if (email != null && !email.isBlank()) {
                p.add(cb.like(cb.lower(root.<String>get("email")), "%" + email.trim().toLowerCase() + "%"));
            }
            if (estudante != null) {
                p.add(cb.equal(root.get("estudante"), estudante));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        return participanteRepository.findAll(spec, Sort.by("nome")).stream()
                .map(ParticipanteMapper::toResponse).toList();
    }

    @Transactional
    public ParticipanteResponseDTO atualizar(Long id, ParticipanteRequestDTO dto) {
        Participante p = buscarEntidade(id);
        if (participanteRepository.existsByEmailIgnoreCaseAndIdNot(dto.email().trim(), id)) {
            throw new RegraNegocioException("Já existe outro participante com o e-mail " + dto.email().trim() + ".");
        }
        ParticipanteMapper.updateEntity(p, dto);
        log.info("Participante atualizado: id={}", id);
        return ParticipanteMapper.toResponse(participanteRepository.save(p));
    }

    @Transactional
    public void excluir(Long id) {
        Participante p = buscarEntidade(id);
        // REGRA: não excluir participante que possua inscrições
        if (inscricaoRepository.existsByParticipanteId(id)) {
            throw new RegraNegocioException("Não é possível excluir um participante que possui inscrições.");
        }
        participanteRepository.delete(p);
        log.info("Participante excluído: id={}", id);
    }

    private Participante buscarEntidade(Long id) {
        return participanteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Participante não encontrado com id " + id));
    }
}
