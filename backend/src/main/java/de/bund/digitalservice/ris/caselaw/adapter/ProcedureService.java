package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitSearchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAProcedureLinkDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAProcedureLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitSearchEntryTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchEntry;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProcedureService {
  private final JPAProcedureRepository repository;
  private final JPAProcedureLinkRepository linkRepository;
  private final DatabaseDocumentationUnitSearchRepository documentUnitRepository;
  private final JPADocumentationOfficeRepository documentationOfficeRepository;

  public ProcedureService(
      JPAProcedureRepository repository,
      JPAProcedureLinkRepository linkRepository,
      DatabaseDocumentationUnitSearchRepository documentUnitRepository,
      JPADocumentationOfficeRepository documentationOfficeRepository) {
    this.repository = repository;
    this.linkRepository = linkRepository;
    this.documentUnitRepository = documentUnitRepository;
    this.documentationOfficeRepository = documentationOfficeRepository;
  }

  public Page<Procedure> search(
      Optional<String> query, DocumentationOffice documentationOffice, Pageable pageable) {
    return repository
        .findByLabelContainingAndDocumentationOffice(
            query, documentationOfficeRepository.findByLabel(documentationOffice.label()), pageable)
        .map(
            dto ->
                Procedure.builder()
                    .label(dto.getLabel())
                    .documentUnitCount(
                        linkRepository.countLatestProcedureLinksByProcedure(dto.getId()))
                    .created_at(dto.getCreatedAt())
                    .build());
  }

  public List<DocumentationUnitSearchEntry> getDocumentUnits(
      String procedureLabel, DocumentationOffice documentationOffice) {
    return linkRepository
        .findLatestProcedureLinksByProcedure(
            repository
                .findByLabelAndDocumentationOfficeOrderByCreatedAtDesc(
                    procedureLabel,
                    documentationOfficeRepository.findByLabel(documentationOffice.label()))
                .getId())
        .stream()
        .map(JPAProcedureLinkDTO::getDocumentationUnitId)
        .map(documentUnitRepository::findById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(DocumentationUnitSearchEntryTransformer::transferDTO)
        .toList();
  }
}
