package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.FmxRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresFmxRepositoryImpl implements FmxRepository {

  private final OriginalXmlRepository dbRepository;

  public PostgresFmxRepositoryImpl(OriginalXmlRepository dbRepository) {
    this.dbRepository = dbRepository;
  }

  @Override
  public String getFmxAsString(UUID documentationUnitUuid) {

    return dbRepository
        .findByDocumentationUnitId(documentationUnitUuid)
        .filter(originalXmlDTO -> OriginalXmlDTO.Type.FMX.equals(originalXmlDTO.type))
        .map(OriginalXmlDTO::getContent)
        .orElse(null);
  }

  @Override
  public void attachFmxToDocumentationUnit(
      UUID documentationUnitUuid, String content, String source) {
    var originalXml =
        OriginalXmlDTO.builder()
            .type(OriginalXmlDTO.Type.FMX)
            .content(content)
            .source(source)
            .documentationUnitId(documentationUnitUuid)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    dbRepository.save(originalXml);
  }
}
