package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentUnitTransformer;
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
}
