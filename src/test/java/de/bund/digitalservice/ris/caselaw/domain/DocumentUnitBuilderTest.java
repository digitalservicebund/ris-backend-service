package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class DocumentUnitBuilderTest {

  @Test
  void shouldConvertCorrectly() {
    DocumentUnitDTO documentUnitDTO = new DocumentUnitDTO();
    documentUnitDTO.setFilename("doc.docx");
    documentUnitDTO.setFileNumbers(List.of("fileNumber123"));
    documentUnitDTO.reasons = "reasons123";
    DocumentUnit documentUnit =
        DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build();

    assertThat(documentUnit.filename()).isEqualTo("doc.docx");
    assertThat(documentUnit.coreData().fileNumber()).isEqualTo("fileNumber123");
    assertThat(documentUnit.texts().reasons()).isEqualTo("reasons123");
  }
}
