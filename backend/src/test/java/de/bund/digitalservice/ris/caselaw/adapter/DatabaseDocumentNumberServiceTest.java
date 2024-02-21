package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.jupiter.api.Assertions.assertThrows;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberRepository;
import de.bund.digitalservice.ris.caselaw.domain.DateUtil;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitExistsException;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Disabled
@Import({DatabaseDocumentNumberService.class})
class DatabaseDocumentNumberServiceTest {

  @Autowired private DocumentNumberService service;

  @MockBean DatabaseDocumentNumberRepository documentNumberRepository;

  @MockBean DocumentUnitRepository documentUnitRepository;

  @Test
  void generateNextDocumentNumber_shouldNotSaveDuplicates() {
    assertThrows(
        DocumentationUnitExistsException.class,
        () -> {
          var documentNumber = "KORE70000" + DateUtil.getYear();
          var abbrivation = "BGH";
          documentUnitRepository.save(
              DocumentUnit.builder()
                  .uuid(UUID.randomUUID())
                  .documentNumber(documentNumber)
                  .build());
          service.execute(abbrivation);
        });
  }
}
