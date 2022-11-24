package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface DocumentUnitRepository {

  Mono<DocumentUnitDTO> findByDocumentnumber(String documentnumber);

  Mono<DocumentUnitDTO> findByUuid(UUID uuid);

  Mono<DocumentUnitDTO> save(DocumentUnitDTO documentUnitDTO);

  Mono<Void> delete(DocumentUnitDTO documentUnitDTO);
}
