package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.XmlPublicationTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Publication;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublication;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublicationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresXmlPublicationRepositoryImpl implements XmlPublicationRepository {

  private final DatabaseXmlPublicationRepository repository;
  private final DatabaseDocumentationUnitRepository documentUnitRepository;

  public PostgresXmlPublicationRepositoryImpl(
      DatabaseXmlPublicationRepository repository,
      DatabaseDocumentationUnitRepository documentUnitRepository) {

    this.repository = repository;
    this.documentUnitRepository = documentUnitRepository;
  }

  @Override
  public XmlPublication save(XmlPublication xmlPublication) {
    DocumentationUnitDTO documentUnitDTO =
        documentUnitRepository.findById(xmlPublication.documentUnitUuid()).orElseThrow();

    XmlPublicationDTO xmlPublicationDTO =
        XmlPublicationTransformer.transformToDTO(xmlPublication, documentUnitDTO.getId());
    xmlPublicationDTO = repository.save(xmlPublicationDTO);

    return XmlPublicationTransformer.transformToDomain(
        xmlPublicationDTO, xmlPublication.documentUnitUuid());
  }

  @Override
  public List<Publication> getPublicationsByDocumentUnitUuid(UUID documentUnitUuid) {
    DocumentationUnitDTO documentUnitDTO =
        documentUnitRepository.findById(documentUnitUuid).orElseThrow();

    List<XmlPublicationDTO> xmlPublicationDTOs =
        repository.findAllByDocumentUnitIdOrderByPublishDateDesc(documentUnitDTO.getId());

    return xmlPublicationDTOs.stream()
        .map(
            xmlPublicationDTO ->
                (Publication)
                    XmlPublicationTransformer.transformToDomain(
                        xmlPublicationDTO, documentUnitUuid))
        .toList();
  }

  @Override
  public XmlPublication getLastXmlPublication(UUID documentUnitUuid) {
    DocumentationUnitDTO documentationUnitDTO =
        documentUnitRepository.findById(documentUnitUuid).orElseThrow();

    XmlPublicationDTO xmlPublicationDTO =
        repository.findTopByDocumentUnitIdOrderByPublishDateDesc(documentationUnitDTO.getId());

    return XmlPublicationTransformer.transformToDomain(xmlPublicationDTO, documentUnitUuid);
  }
}
