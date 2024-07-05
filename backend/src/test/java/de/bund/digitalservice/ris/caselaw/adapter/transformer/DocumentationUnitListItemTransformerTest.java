package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitListItemDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DocumentationUnitListItemTransformerTest {
  @Test
  void testTransformToDomain_withStatus_shouldTransformStatus() {
    UUID id = UUID.randomUUID();
    List<StatusDTO> statusList =
        List.of(
            StatusDTO.builder()
                .publicationStatus(PublicationStatus.PUBLISHED)
                .withError(false)
                .build());
    DocumentationUnitListItemDTO currentDto =
        DocumentationUnitDTO.builder().id(id).status(statusList).build();

    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItemTransformer.transformToDomain(currentDto);

    assertThat(documentationUnitListItem.referencedDocumentationUnitId()).isEqualTo(id);
    assertThat(documentationUnitListItem.status().publicationStatus())
        .isEqualTo(PublicationStatus.PUBLISHED);
    assertThat(documentationUnitListItem.status().withError()).isFalse();
  }

  @Test
  void testTransformToDomain_withStatus_shouldTransformLatestStatus() {
    UUID id = UUID.randomUUID();
    List<StatusDTO> statusList =
        List.of(
            StatusDTO.builder()
                .publicationStatus(PublicationStatus.DELETING)
                .withError(true)
                .createdAt(Instant.parse("2020-01-01T01:01:01.00Z"))
                .build(),
            StatusDTO.builder()
                .publicationStatus(PublicationStatus.LOCKED)
                .withError(false)
                .createdAt(Instant.parse("2024-01-01T01:01:01.00Z"))
                .build());

    DocumentationUnitListItemDTO currentDto =
        DocumentationUnitDTO.builder().id(id).status(statusList).build();

    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItemTransformer.transformToDomain(currentDto);

    assertThat(documentationUnitListItem.referencedDocumentationUnitId()).isEqualTo(id);
    assertThat(documentationUnitListItem.status().publicationStatus())
        .isEqualTo(PublicationStatus.LOCKED);
    assertThat(documentationUnitListItem.status().withError()).isFalse();
  }

  @Test
  void testTransformToDomain_withoutStatus_shouldTransformToNullStatus() {
    UUID id = UUID.randomUUID();
    List<StatusDTO> statusList = List.of();

    DocumentationUnitListItemDTO currentDto =
        DocumentationUnitDTO.builder().id(id).status(statusList).build();

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
