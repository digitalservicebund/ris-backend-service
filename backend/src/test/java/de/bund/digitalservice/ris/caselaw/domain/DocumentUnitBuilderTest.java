package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformer;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class DocumentUnitBuilderTest {
  @Test
  void shouldConvertDocumentationUnitCorrectly() {
    DocumentationUnitDTO documentationUnitDTO = new DocumentationUnitDTO();
    documentationUnitDTO.setAttachments(
        Collections.singletonList(AttachmentDTO.builder().filename("doc.docx").build()));
    documentationUnitDTO.setGrounds("reasons123");
    documentationUnitDTO.setDocumentationOffice(DocumentationOfficeDTO.builder().build());
    DocumentUnit documentUnit =
        DocumentationUnitTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentUnit.attachments().get(0).name()).isEqualTo("doc.docx");
    assertThat(documentUnit.texts().reasons()).isEqualTo("reasons123");
  }
}
