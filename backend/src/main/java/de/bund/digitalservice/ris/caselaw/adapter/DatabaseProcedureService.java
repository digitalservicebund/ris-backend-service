package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitListItemTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ProcedureTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseProcedureService implements ProcedureService {
  private final DatabaseProcedureRepository repository;
  private final DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  public DatabaseProcedureService(
      DatabaseProcedureRepository repository,
      DatabaseDocumentationOfficeRepository documentationOfficeRepository) {
    this.repository = repository;
    this.documentationOfficeRepository = documentationOfficeRepository;
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Page<Procedure> search(
      Optional<String> query, DocumentationOffice documentationOffice, Pageable pageable) {

    DocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation());

    return query
        .map(
            queryString ->
                repository.findAllByLabelContainingAndDocumentationOfficeOrderByCreatedAtDesc(
                    queryString.trim(), documentationOfficeDTO, pageable))
        .orElse(
            repository.findAllByDocumentationOfficeOrderByCreatedAtDesc(
                documentationOfficeDTO, pageable))
        .map(ProcedureTransformer::transformToDomain);
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public List<DocumentationUnitListItem> getDocumentUnits(UUID procedureId) {
    return repository
        .findById(procedureId)
        .map(
            procedureDTO ->
                procedureDTO.getDocumentationUnits().stream()
                    .filter(
                        documentationUnitDTO -> {
                          List<DocumentationUnitProcedureDTO> procedures =
                              documentationUnitDTO.getProcedures();
                          return procedures
                              .get(procedures.size() - 1)
                              .getProcedure()
                              .equals(procedureDTO);
                        })
                    .distinct()
                    .map(DocumentationUnitListItemTransformer::transformToDomain)
                    .map(documentationUnitListItem -> documentationUnitListItem.toBuilder().build())
                    .toList())
        .orElse(null);
  }

  @Override
  public void delete(UUID procedureId) {
    repository.deleteById(procedureId);
  }
}
