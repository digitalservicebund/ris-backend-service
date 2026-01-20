package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DecisionTransformer;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class DecisionBuilderTest {
  @Test
  void shouldConvertDecisionCorrectly() {
    DecisionDTO documentationUnitDTO = new DecisionDTO();
    documentationUnitDTO.setAttachments(
        Collections.singletonList(
            AttachmentDTO.builder()
                .filename("doc.docx")
                .format("docx")
                .attachmentType(AttachmentType.ORIGINATING.name())
                .build()));
    documentationUnitDTO.setGrounds("reasons123");
    documentationUnitDTO.setDocumentationOffice(DocumentationOfficeDTO.builder().build());
    Decision decision = DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(decision.attachments().get(0).name()).isEqualTo("doc.docx");
    assertThat(decision.longTexts().reasons()).isEqualTo("reasons123");
  }
}
