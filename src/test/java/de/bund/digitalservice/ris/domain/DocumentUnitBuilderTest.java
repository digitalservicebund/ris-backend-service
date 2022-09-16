package de.bund.digitalservice.ris.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class DocumentUnitBuilderTest {

  @Test
  void shouldConvertCorrectly() {
    DocUnitDTO docUnitDTO = new DocUnitDTO();
    docUnitDTO.setFileNumber("fileNumber123");
    docUnitDTO.reasons = "reasons123";
    DocumentUnit documentUnit = DocumentUnitBuilder.newInstance().setDocUnitDTO(docUnitDTO).build();

    assertThat(documentUnit.coreData().fileNumber()).isEqualTo("fileNumber123");
    assertThat(documentUnit.categories().reasons()).isEqualTo("reasons123");
  }
}
