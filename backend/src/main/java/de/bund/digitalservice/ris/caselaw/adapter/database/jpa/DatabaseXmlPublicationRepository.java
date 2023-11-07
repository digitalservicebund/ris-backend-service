package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseXmlPublicationRepository extends JpaRepository<XmlPublicationDTO, Long> {

  XmlPublicationDTO findTopByDocumentUnitIdOrderByPublishDateDesc(UUID documentUnitId);

  List<XmlPublicationDTO> findAllByDocumentUnitIdOrderByPublishDateDesc(UUID documentUnitId);
}
