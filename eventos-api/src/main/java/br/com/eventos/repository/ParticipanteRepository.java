package br.com.eventos.repository;

import br.com.eventos.model.Participante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ParticipanteRepository extends JpaRepository<Participante, Long>, JpaSpecificationExecutor<Participante> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
