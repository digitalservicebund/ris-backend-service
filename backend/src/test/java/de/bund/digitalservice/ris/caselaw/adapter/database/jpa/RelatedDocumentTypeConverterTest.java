package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType;
import org.junit.jupiter.api.Test;

class RelatedDocumentTypeConverterTest {
  @Test
  void testConvertToDatabaseColumn_withTypeActiveCitation_shouldReturnCaselawActiveCitation() {
    RelatedDocumentTypeConverter converter = new RelatedDocumentTypeConverter();

    String result = converter.convertToDatabaseColumn(RelatedDocumentationType.ACTIVE_CITATION);

    assertThat(result).isEqualTo("caselaw_active_citation");
  }

  @Test
  void testConvertToDatabaseColumn_withTypeEnsuingDecision_shouldReturnEnsuingDecision() {
    RelatedDocumentTypeConverter converter = new RelatedDocumentTypeConverter();

    String result = converter.convertToDatabaseColumn(RelatedDocumentationType.ENSUING_DECISION);

    assertThat(result).isEqualTo("ensuing_decision");
  }

  @Test
  void testConvertToDatabaseColumn_withTypePendingDecision_shouldReturnPendingDecision() {
    RelatedDocumentTypeConverter converter = new RelatedDocumentTypeConverter();

    String result = converter.convertToDatabaseColumn(RelatedDocumentationType.PENDING_DECISION);

    assertThat(result).isEqualTo("pending_decision");
  }

  @Test
  void testConvertToDatabaseColumn_withTypePreviousDecision_shouldReturnPreviousDecision() {
    RelatedDocumentTypeConverter converter = new RelatedDocumentTypeConverter();

    String result = converter.convertToDatabaseColumn(RelatedDocumentationType.PREVIOUS_DECISION);

    assertThat(result).isEqualTo("previous_decision");
  }

  @Test
  void testConvertToDatabaseColumn_withNull_shouldReturnNull() {
    RelatedDocumentTypeConverter converter = new RelatedDocumentTypeConverter();

    String result = converter.convertToDatabaseColumn(null);

    assertThat(result).isEqualTo(null);
  }

  @Test
  void testConvertToEntityAttribute_withTypeCaselawActiveCitation_shouldReturnActiveCitation() {
    RelatedDocumentTypeConverter converter = new RelatedDocumentTypeConverter();

    RelatedDocumentationType result = converter.convertToEntityAttribute("caselaw_active_citation");

    assertThat(result).isEqualTo(RelatedDocumentationType.ACTIVE_CITATION);
  }

  @Test
  void testConvertToEntityAttribute_withTypeEnsuingDecision_shouldReturnEnsuingDecision() {
    RelatedDocumentTypeConverter converter = new RelatedDocumentTypeConverter();

    RelatedDocumentationType result = converter.convertToEntityAttribute("ensuing_decision");

    assertThat(result).isEqualTo(RelatedDocumentationType.ENSUING_DECISION);
  }

  @Test
  void testConvertToEntityAttribute_withTypePendingDecision_shouldReturnPendingDecision() {
    RelatedDocumentTypeConverter converter = new RelatedDocumentTypeConverter();

    RelatedDocumentationType result = converter.convertToEntityAttribute("pending_decision");

    assertThat(result).isEqualTo(RelatedDocumentationType.PENDING_DECISION);
  }

  @Test
  void testConvertToEntityAttribute_withTypePreviousDecision_shouldReturnPreviousDecision() {
    RelatedDocumentTypeConverter converter = new RelatedDocumentTypeConverter();

    RelatedDocumentationType result = converter.convertToEntityAttribute("previous_decision");

    assertThat(result).isEqualTo(RelatedDocumentationType.PREVIOUS_DECISION);
  }

  @Test
  void testConvertToEntityAttribute_withTypeNull_shouldReturnNull() {
    RelatedDocumentTypeConverter converter = new RelatedDocumentTypeConverter();

    RelatedDocumentationType result = converter.convertToEntityAttribute(null);

    assertThat(result).isEqualTo(null);
  }

  @Test
  void testConvertToEntityAttribute_withUnknownType_shouldThrowIllegalArgumentException() {
    RelatedDocumentTypeConverter converter = new RelatedDocumentTypeConverter();

    assertThatThrownBy(() -> converter.convertToEntityAttribute("unknown type"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
