package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.AttachmentInlineTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentInlineRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresAttachmentInlineRepositoryImpl implements AttachmentInlineRepository {
  private final DatabaseAttachmentInlineRepository repository;

  public PostgresAttachmentInlineRepositoryImpl(DatabaseAttachmentInlineRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<Attachment> findAllByDocumentationUnitId(UUID documentationUnitId) {
    return repository.findAllByDocumentationUnitId(documentationUnitId).stream()
        .map(AttachmentInlineTransformer::transformToDomain)
        .toList();
  }

  @Override
  public Optional<Attachment> findByDocumentationUnitIdAndFilename(
      UUID documentationUnitId, String fileName) {
    return repository
        .findByDocumentationUnitIdAndFilename(documentationUnitId, fileName)
        .map(AttachmentInlineTransformer::transformToDomain);
  }
}
