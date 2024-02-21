package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberRepository;
import de.bund.digitalservice.ris.caselaw.domain.DateUtil;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitExistsException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Disabled("Due to injection not working")
@Import({DatabaseDocumentNumberService.class, DocumentNumberPatternConfig.class})
@TestPropertySource(properties = {"neuris.document-number-patterns.bgh=BGH"})
class DatabaseDocumentNumberServiceTest {

  @Autowired private DocumentNumberService service;

  @MockBean DatabaseDocumentNumberRepository documentNumberRepository;

  @MockBean DocumentUnitRepository documentUnitRepository;

  @Test
  void generateNextDocumentNumber_shouldNotSaveDuplicates() {
    var documentNumber = "KORE70000" + DateUtil.getYear();
    var abbrivation = "BGH";

    // when(documentUnitRepository.findByDocumentNumber(documentNumber)).thenReturn
    // (DocumentUnit.builder().uuid(UUID.randomUUID()).documentNumber(documentNumber).build());
    assertThatThrownBy(
            () -> {
              service.execute(abbrivation);
            })
        .isInstanceOf(DocumentationUnitExistsException.class)
        .hasMessageContaining("Index: 2, Size: 2");
  }
}
