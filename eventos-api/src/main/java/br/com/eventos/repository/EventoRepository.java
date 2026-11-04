package br.com.eventos.repository;

import br.com.eventos.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;

public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {

    boolean existsByNomeIgnoreCaseAndDataInicio(String nome, LocalDateTime dataInicio);

    boolean existsByNomeIgnoreCaseAndDataInicioAndIdNot(String nome, LocalDateTime dataInicio, Long id);
}
