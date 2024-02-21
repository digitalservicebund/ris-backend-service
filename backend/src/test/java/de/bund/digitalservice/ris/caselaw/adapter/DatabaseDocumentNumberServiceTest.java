package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.DateUtil;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitExistsException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = DocumentNumberPatternConfig.class)
@Import(DatabaseDocumentNumberService.class)
class DatabaseDocumentNumberServiceTest {

  @Autowired DocumentNumberPatternConfig documentNumberPatternConfig;
  @MockBean DatabaseDocumentNumberRepository databaseDocumentNumberRepository;
  @MockBean DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;

  @Autowired DatabaseDocumentNumberService service;

  @Test
  void generateNextDocumentNumber_shouldNotSaveDuplicates() {
    var documentNumber = "KORE70000" + DateUtil.getYear();
    var nextNumber = "KORE70001" + DateUtil.getYear();
    var abbreviation = "BGH";
    var documentationUnitDTO =
        DocumentationUnitDTO.builder().id(UUID.randomUUID()).documentNumber(documentNumber).build();
    when(databaseDocumentationUnitRepository.findByDocumentNumber(nextNumber))
        .thenReturn(Optional.of(documentationUnitDTO));

    assertThatThrownBy(
            () -> {
              service.execute(abbreviation);
            })
        .isInstanceOf(DocumentationUnitExistsException.class)
        .hasMessageContaining("Document Number already exists: " + nextNumber);
  }
}
