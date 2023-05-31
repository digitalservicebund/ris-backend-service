package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitWriteDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentUnitTransformer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class DocumentUnitBuilderTest {

  @Test
  void shouldConvertCorrectly() {
    // TODO: check not all fields?
    DocumentUnitWriteDTO documentUnitWriteDTO = new DocumentUnitWriteDTO();
    documentUnitWriteDTO.setFilename("doc.docx");
    documentUnitWriteDTO.setReasons("reasons123");
    DocumentUnit documentUnit = DocumentUnitTransformer.transformDTO(documentUnitWriteDTO);

    assertThat(documentUnit.filename()).isEqualTo("doc.docx");
    assertThat(documentUnit.texts().reasons()).isEqualTo("reasons123");
  }
}
