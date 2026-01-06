package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentInlineRepository extends JpaRepository<AttachmentInlineDTO, UUID> {
  List<AttachmentInlineDTO> findAllByDocumentationUnitId(UUID documentationUnitId);

  Optional<AttachmentInlineDTO> findByDocumentationUnitIdAndFilename(
      UUID documentationUnitId, String fileName);
}
