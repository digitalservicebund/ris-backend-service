package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentDTO, UUID> {
  List<AttachmentDTO> findAllByDocumentationUnitId(UUID documentationUnitId);

  Optional<AttachmentDTO> findByS3ObjectPath(String s3ObjectPath);
}
