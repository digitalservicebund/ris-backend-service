package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberFormatterException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberPatternException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Disabled
@Import({DatabaseDocumentNumberService.class, DocumentNumberPatternConfig.class})
class DatabaseDocumentNumberServiceTest {

  @Autowired private DocumentNumberService service;
  @MockBean DatabaseDocumentNumberRepository documentNumberRepository;

  @Test
  void generateNextDocumentNumber_shouldNotSaveDuplicates()
      throws DocumentNumberPatternException, DocumentNumberFormatterException {
    service.generateNextAvailableDocumentNumber(DocumentationOffice.builder().build());
  }
}
