package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginalFileDocumentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentUnitTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class DocumentUnitBuilderTest {

  @Test
  void shouldConvertCorrectly() {
    // TODO: check not all fields?
    DocumentUnitDTO documentUnitDTO = new DocumentUnitDTO();
    documentUnitDTO.setFilename("doc.docx");
    documentUnitDTO.setReasons("reasons123");
    DocumentUnit documentUnit = DocumentUnitTransformer.transformDTO(documentUnitDTO);

    assertThat(documentUnit.filename()).isEqualTo("doc.docx");
    assertThat(documentUnit.texts().reasons()).isEqualTo("reasons123");
  }

  @Test
  void shouldConvertDocumentationUnitCorrectly() {
    // TODO: check not all fields?
    DocumentationUnitDTO documentUnitDTO = new DocumentationUnitDTO();
    documentUnitDTO.setOriginalFileDocument(
        OriginalFileDocumentDTO.builder().filename("doc.docx").build());
    documentUnitDTO.setGrounds("reasons123");
    DocumentUnit documentUnit = DocumentationUnitTransformer.transformDTO(documentUnitDTO);

    assertThat(documentUnit.filename()).isEqualTo("doc.docx");
    assertThat(documentUnit.texts().reasons()).isEqualTo("reasons123");
  }
}
