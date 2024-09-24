package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentationOfficeService {
  private final DocumentationOfficeRepository documentationOfficeRepository;

  public DocumentationOfficeService(DocumentationOfficeRepository documentationOfficeRepository) {
    this.documentationOfficeRepository = documentationOfficeRepository;
  }

  public List<DocumentationOffice> getAll() {
    return documentationOfficeRepository.findAll();
  }
}
