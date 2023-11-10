package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class RelatedDocumentTypeConverter
    implements AttributeConverter<RelatedDocumentationType, String> {
  @Override
  public String convertToDatabaseColumn(RelatedDocumentationType type) {
    if (type == null) {
      return null;
    }
    return type.getDatabaseValue();
  }

  @Override
  public RelatedDocumentationType convertToEntityAttribute(String type) {
    if (type == null) {
      return null;
    }

    return Stream.of(RelatedDocumentationType.values())
        .filter(c -> c.getDatabaseValue().equals(type))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }
}
