package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.XmlPublicationTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Publication;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublication;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublicationRepository;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresXmlPublicationRepositoryImpl implements XmlPublicationRepository {

  private final DatabaseXmlPublicationRepository repository;
  private final DatabaseDocumentUnitRepository documentUnitRepository;

  public PostgresXmlPublicationRepositoryImpl(
      DatabaseXmlPublicationRepository repository,
      DatabaseDocumentUnitRepository documentUnitRepository) {

    this.repository = repository;
    this.documentUnitRepository = documentUnitRepository;
  }

  @Override
  public Mono<XmlPublication> save(XmlPublication xmlPublication) {
    return documentUnitRepository
        .findByUuid(xmlPublication.documentUnitUuid())
        .map(
            documentUnitDTO ->
                XmlPublicationTransformer.transformToDTO(xmlPublication, documentUnitDTO.getId()))
        .flatMap(repository::save)
        .map(
            xmlPublicationDTO ->
                XmlPublicationTransformer.transformToDomain(
                    xmlPublicationDTO, xmlPublication.documentUnitUuid()));
  }

  @Override
  public Flux<Publication> getPublicationsByDocumentUnitUuid(UUID documentUnitUuid) {
    return documentUnitRepository
        .findByUuid(documentUnitUuid)
        .flatMapMany(
            documentUnitDTO ->
                repository.findAllByDocumentUnitIdOrderByPublishDateDesc(documentUnitDTO.getId()))
        .map(
            xmlPublicationDTO ->
                XmlPublicationTransformer.transformToDomain(xmlPublicationDTO, documentUnitUuid));
  }

  @Override
  public Mono<XmlPublication> getLastXmlPublication(UUID documentUnitUuid) {
    return documentUnitRepository
        .findByUuid(documentUnitUuid)
        .flatMap(
            documentUnitDTO ->
                repository.findTopByDocumentUnitIdOrderByPublishDateDesc(documentUnitDTO.getId()))
        .map(
            xmlPublicationDTO ->
                XmlPublicationTransformer.transformToDomain(xmlPublicationDTO, documentUnitUuid));
  }
}
