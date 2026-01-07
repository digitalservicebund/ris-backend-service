package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentS3Repository extends JpaRepository<AttachmentS3DTO, UUID> {
  void deleteByS3ObjectPath(String s3ObjectPath);

  Optional<AttachmentS3DTO> findByS3ObjectPath(String s3ObjectPath);

  List<AttachmentS3DTO> findAllByDocumentationUnitId(UUID documentationUnitId);

  Optional<AttachmentS3DTO> findByDocumentationUnitIdAndFilename(
      UUID documentationUnitId, String fileName);
}
