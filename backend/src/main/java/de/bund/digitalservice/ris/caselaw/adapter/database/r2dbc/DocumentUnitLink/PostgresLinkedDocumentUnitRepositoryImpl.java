package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitLink;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitLink.DatabaseLinkedDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberRepository;
import de.bund.digitalservice.ris.caselaw.domain.LinkedDocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.LinkedDocumentUnitRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class PostgresLinkedDocumentUnitRepositoryImpl implements LinkedDocumentUnitRepository {

  private final DatabaseLinkedDocumentUnitRepository repository;
  private final DatabaseDocumentUnitLinkRepository linkRepository;

  public PostgresLinkedDocumentUnitRepositoryImpl(
      DatabaseLinkedDocumentUnitRepository repository, DatabaseDocumentUnitLinkRepository linkRepository) {
    this.repository = repository;
    this.linkRepository = linkRepository;
  }

  @Override
  public Flux<LinkedDocumentUnit> findAllByDocumentUnitId(Long id) {
    return linkRepository.findAllByParentDocumentUnitId(id).map(documentUnitLinkDTO -> repository.findById(documentUnitLinkDTO.childDocumentUnitId))

    return repository
        .findAllById(id)
        .map(
            linkedDocumentUnitDTO ->
                LinkedDocumentUnit.builder()
                    .id(linkedDocumentUnitDTO.id)
                    .fileNumber(linkedDocumentUnitDTO.fileNumbers)
                    .court(linkedDocumentUnitDTO.getCourt()));
  }
}
