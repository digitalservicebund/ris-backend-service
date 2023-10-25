package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitSearchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureLinkDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitSearchEntryTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchEntry;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseProcedureService implements ProcedureService {
  private final DatabaseProcedureRepository repository;
  private final DatabaseProcedureLinkRepository linkRepository;
  private final DatabaseDocumentationUnitSearchRepository documentUnitRepository;
  private final DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  public DatabaseProcedureService(
      DatabaseProcedureRepository repository,
      DatabaseProcedureLinkRepository linkRepository,
      DatabaseDocumentationUnitSearchRepository documentUnitRepository,
      DatabaseDocumentationOfficeRepository documentationOfficeRepository) {
    this.repository = repository;
    this.linkRepository = linkRepository;
    this.documentUnitRepository = documentUnitRepository;
    this.documentationOfficeRepository = documentationOfficeRepository;
  }

  @Override
  public Page<Procedure> search(
      Optional<String> query, DocumentationOffice documentationOffice, Pageable pageable) {
    return repository
        .findByLabelContainingAndDocumentationOffice(
            query,
            documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation()),
            pageable)
        .map(
            dto ->
                Procedure.builder()
                    .label(dto.getLabel())
                    .documentUnitCount(
                        linkRepository.countLatestProcedureLinksByProcedure(dto.getId()))
                    .createdAt(dto.getCreatedAt())
                    .build());
  }

  @Override
  public List<DocumentationUnitSearchEntry> getDocumentUnits(
      String procedureLabel, DocumentationOffice documentationOffice) {
    return linkRepository
        .findLatestProcedureLinksByProcedure(
            repository
                .findByLabelAndDocumentationOffice(
                    procedureLabel,
                    documentationOfficeRepository.findByAbbreviation(
                        documentationOffice.abbreviation()))
                .getId())
        .stream()
        .map(ProcedureLinkDTO::getDocumentationUnitId)
        .map(documentUnitRepository::findById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(DocumentationUnitSearchEntryTransformer::transferDTO)
        .toList();
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public void delete(String procedureLabel, DocumentationOffice documentationOffice) {
    repository.deleteByLabelAndDocumentationOffice(
        procedureLabel,
        documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation()));
  }
}
