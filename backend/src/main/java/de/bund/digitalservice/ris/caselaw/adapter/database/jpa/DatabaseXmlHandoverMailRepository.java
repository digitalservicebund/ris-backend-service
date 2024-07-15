package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseXmlHandoverMailRepository extends JpaRepository<XmlHandoverMailDTO, Long> {

  // TODO findTopByDocumentUnitIdOrderByCreatedDateDesc
  XmlHandoverMailDTO findTopByDocumentUnitIdOrderByCreatedDateDesc(UUID documentUnitId);

  // TODO findAllByDocumentUnitIdOrderByCreatedDateDesc
  List<XmlHandoverMailDTO> findAllByDocumentUnitIdOrderByCreatedDateDesc(UUID documentUnitId);
}
