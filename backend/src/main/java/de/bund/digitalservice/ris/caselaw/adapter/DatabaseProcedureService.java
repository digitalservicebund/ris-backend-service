package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitSearchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ProcedureTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    DocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation());

    return query
        .map(
            queryString ->
                repository.findAllByLabelContainingAndDocumentationOffice(
                    queryString, documentationOfficeDTO, pageable))
        .orElse(repository.findAllByDocumentationOffice(documentationOfficeDTO, pageable))
        .map(ProcedureTransformer::transformToDomain);

    //    return repository
    //        .findByLabelContainingAndDocumentationOffice(
    //            query,
    //
    // documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation()),
    //            pageable);
  }

  @Override
  public List<DocumentUnit> getDocumentUnits(UUID procedureId) {
    return repository
        .findById(procedureId)
        .map(
            procedureDTO ->
                procedureDTO.getDocumentationUnits().stream()
                    .map(DocumentationUnitTransformer::transformToDomain)
                    .toList())
        .orElse(null);
    //    return linkRepository
    //        .findLatestProcedureLinksByProcedure(
    //            repository
    //                .findByLabelAndDocumentationOffice(
    //                    procedureLabel,
    //                    documentationOfficeRepository.findByAbbreviation(
    //                        documentationOffice.abbreviation()))
    //                .getId())
    //        .stream()
    //        .map(ProcedureLinkDTO::getDocumentationUnitDTO)
    //        .map(DocumentationUnitTransformer::transformToDomain)
    //        .toList();
  }

  @Override
  public void delete(UUID procedureId) {
    repository.deleteById(procedureId);
  }
}
