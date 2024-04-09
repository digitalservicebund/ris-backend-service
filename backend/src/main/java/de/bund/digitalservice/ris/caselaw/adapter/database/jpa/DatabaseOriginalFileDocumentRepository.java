package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseOriginalFileDocumentRepository
    extends JpaRepository<OriginalFileDocumentDTO, UUID> {
  void deleteByS3ObjectPath(String s3ObjectPath);

  void deleteAllByDocumentationUnitId(UUID documentationUnitId);

  List<OriginalFileDocumentDTO> findAllByDocumentationUnitId(UUID documentationUnitId);
}
