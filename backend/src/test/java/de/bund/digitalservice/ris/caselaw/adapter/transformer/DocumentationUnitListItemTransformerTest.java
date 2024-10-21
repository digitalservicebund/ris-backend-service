package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitListItemDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DocumentationUnitListItemTransformerTest {
  @Test
  void testTransformToDomain_withStatus_shouldTransformStatus() {
    UUID id = UUID.randomUUID();
    DocumentationUnitListItemDTO currentDto =
        DocumentationUnitDTO.builder()
            .id(id)
            .status(
                StatusDTO.builder()
                    .createdAt(Instant.now())
                    .publicationStatus(PublicationStatus.PUBLISHED)
                    .withError(false)
                    .build())
            .build();

    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItemTransformer.transformToDomain(currentDto);

    assertThat(documentationUnitListItem.referencedDocumentationUnitId()).isEqualTo(id);
    assertThat(documentationUnitListItem.status().publicationStatus())
        .isEqualTo(PublicationStatus.PUBLISHED);
    assertThat(documentationUnitListItem.status().withError()).isFalse();
  }

  @Test
  void testTransformToDomain_withoutStatus_shouldTransformToNullStatus() {
    UUID id = UUID.randomUUID();

    DocumentationUnitListItemDTO currentDto =
        DocumentationUnitDTO.builder().id(id).status(null).build();

    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItemTransformer.transformToDomain(currentDto);

    assertThat(documentationUnitListItem.referencedDocumentationUnitId()).isEqualTo(id);
    assertThat(documentationUnitListItem.status()).isNull();
  }

  @Test
  void testTransformToDomain_withNote_shouldHaveNote() {
    UUID id = UUID.randomUUID();
    DocumentationUnitListItemDTO currentDto =
        DocumentationUnitDTO.builder().id(id).note("a note").build();

    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItemTransformer.transformToDomain(currentDto);

    assertThat(documentationUnitListItem.referencedDocumentationUnitId()).isEqualTo(id);
    assertThat(documentationUnitListItem.hasNote()).isTrue();
  }

  @Test
  void testTransformToDomain_withoutNote_shouldHaveNoNote() {
    UUID id = UUID.randomUUID();
    DocumentationUnitListItemDTO currentDto =
        DocumentationUnitDTO.builder().id(id).note(null).build();

    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItemTransformer.transformToDomain(currentDto);

    assertThat(documentationUnitListItem.referencedDocumentationUnitId()).isEqualTo(id);
    assertThat(documentationUnitListItem.hasNote()).isFalse();
  }

  @Test
  void testTransformToDomain_withoutEmptyNote_shouldHaveNoNote() {
    UUID id = UUID.randomUUID();
    DocumentationUnitListItemDTO currentDto =
        DocumentationUnitDTO.builder().id(id).note("").build();

    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItemTransformer.transformToDomain(currentDto);

    assertThat(documentationUnitListItem.referencedDocumentationUnitId()).isEqualTo(id);
    assertThat(documentationUnitListItem.hasNote()).isFalse();
  }
}
