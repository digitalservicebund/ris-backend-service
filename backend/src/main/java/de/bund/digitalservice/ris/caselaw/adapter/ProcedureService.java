package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAProcedureRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProcedureService {
  private final JPAProcedureRepository repository;
  private final JPADocumentationOfficeRepository documentationOfficeRepository;

  public ProcedureService(
      JPAProcedureRepository repository,
      JPADocumentationOfficeRepository documentationOfficeRepository) {
    this.repository = repository;
    this.documentationOfficeRepository = documentationOfficeRepository;
  }

  public List<Procedure> search(Optional<String> query, DocumentationOffice documentationOffice) {
    return repository
        .findByLabelContainingAndDocumentationOffice(
            query, documentationOfficeRepository.findByLabel(documentationOffice.label()))
        .stream()
        .map(dto -> Procedure.builder().label(dto.getLabel()).build())
        .toList();
  }
}
