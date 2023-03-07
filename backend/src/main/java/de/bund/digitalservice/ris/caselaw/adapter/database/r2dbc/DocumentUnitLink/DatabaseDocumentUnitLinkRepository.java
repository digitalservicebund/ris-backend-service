package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitLink;

import java.util.List;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DatabaseDocumentUnitLinkRepository extends R2dbcRepository<DocumentUnitLinkDTO, Long> {
  Flux<DocumentUnitLinkDTO> findAllByParentDocumentUnitId(Long Id);
  Flux<DocumentUnitLinkDTO> saveAll(List<DocumentUnitLinkDTO> links);
}
