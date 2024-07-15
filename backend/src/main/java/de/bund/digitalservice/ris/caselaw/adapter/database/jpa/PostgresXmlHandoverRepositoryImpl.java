package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.XmlPublicationTransformer;
import de.bund.digitalservice.ris.caselaw.domain.XmlHandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.XmlHandoverRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresXmlHandoverRepositoryImpl implements XmlHandoverRepository {

  private final DatabaseXmlHandoverMailRepository repository;
  private final DatabaseDocumentationUnitRepository documentUnitRepository;

  public PostgresXmlHandoverRepositoryImpl(
      DatabaseXmlHandoverMailRepository repository,
      DatabaseDocumentationUnitRepository documentUnitRepository) {

    this.repository = repository;
    this.documentUnitRepository = documentUnitRepository;
  }

  @Override
  public XmlHandoverMail save(XmlHandoverMail xmlHandoverMail) {
    DocumentationUnitDTO documentUnitDTO =
        documentUnitRepository.findById(xmlHandoverMail.documentUnitUuid()).orElseThrow();

    XmlHandoverMailDTO xmlHandoverMailDTO =
        XmlPublicationTransformer.transformToDTO(xmlHandoverMail, documentUnitDTO.getId());
    xmlHandoverMailDTO = repository.save(xmlHandoverMailDTO);

    return XmlPublicationTransformer.transformToDomain(
        xmlHandoverMailDTO, xmlHandoverMail.documentUnitUuid());
  }

  @Override
  public List<XmlHandoverMail> getHandoversByDocumentUnitUuid(UUID documentUnitUuid) {
    DocumentationUnitDTO documentUnitDTO =
        documentUnitRepository.findById(documentUnitUuid).orElseThrow();

    List<XmlHandoverMailDTO> xmlHandoverMailDTOS =
        repository.findAllByDocumentUnitIdOrderByCreatedDateDesc(documentUnitDTO.getId());

    return xmlHandoverMailDTOS.stream()
        .map(
            xmlHandoverMailDTO ->
                XmlPublicationTransformer.transformToDomain(xmlHandoverMailDTO, documentUnitUuid))
        .toList();
  }

  @Override
  public XmlHandoverMail getLastXmlHandoverMail(UUID documentUnitUuid) {
    DocumentationUnitDTO documentationUnitDTO =
        documentUnitRepository.findById(documentUnitUuid).orElseThrow();

    XmlHandoverMailDTO xmlHandoverMailDTO =
        repository.findTopByDocumentUnitIdOrderByCreatedDateDesc(documentationUnitDTO.getId());

    return XmlPublicationTransformer.transformToDomain(xmlHandoverMailDTO, documentUnitUuid);
  }
}
