package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.XmlMailTransformer;
import de.bund.digitalservice.ris.caselaw.domain.MailResponse;
import de.bund.digitalservice.ris.caselaw.domain.XmlMail;
import de.bund.digitalservice.ris.caselaw.domain.XmlMailRepository;
import de.bund.digitalservice.ris.caselaw.domain.XmlMailResponse;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class PostgresXmlMailRepositoryImpl implements XmlMailRepository {

  private final DatabaseXmlMailRepository repository;
  private final DatabaseDocumentUnitReadRepository documentUnitRepository;

  public PostgresXmlMailRepositoryImpl(
      DatabaseXmlMailRepository repository,
      DatabaseDocumentUnitReadRepository documentUnitRepository) {

    this.repository = repository;
    this.documentUnitRepository = documentUnitRepository;
  }

  @Override
  public Mono<XmlMail> save(XmlMail xmlMail) {
    return documentUnitRepository
        .findByUuid(xmlMail.documentUnitUuid())
        .map(documentUnitDTO -> XmlMailTransformer.transformToDTO(xmlMail, documentUnitDTO.getId()))
        .flatMap(repository::save)
        .map(
            xmlMailDTO ->
                XmlMailTransformer.transformToDomain(xmlMailDTO, xmlMail.documentUnitUuid()));
  }

  @Override
  public Mono<MailResponse> getLastPublishedMailResponse(UUID documentUnitUuid) {
    return documentUnitRepository
        .findByUuid(documentUnitUuid)
        .flatMap(
            documentUnitDTO ->
                repository.findTopByDocumentUnitIdOrderByPublishDateDesc(documentUnitDTO.getId()))
        .map(
            xmlMailDTO ->
                new XmlMailResponse(
                    documentUnitUuid,
                    XmlMailTransformer.transformToDomain(xmlMailDTO, documentUnitUuid)));
  }

  @Override
  public Mono<XmlMail> getLastPublishedXmlMail(UUID documentUnitUuid) {
    return documentUnitRepository
        .findByUuid(documentUnitUuid)
        .flatMap(
            documentUnitDTO ->
                repository.findTopByDocumentUnitIdOrderByPublishDateDesc(documentUnitDTO.getId()))
        .map(xmlMailDTO -> XmlMailTransformer.transformToDomain(xmlMailDTO, documentUnitUuid));
  }
}
