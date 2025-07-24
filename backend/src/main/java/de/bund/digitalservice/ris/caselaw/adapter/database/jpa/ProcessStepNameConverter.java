package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.ProcessStepName;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProcessStepNameConverter implements AttributeConverter<ProcessStepName, String> {
  @Override
  public String convertToDatabaseColumn(ProcessStepName attribute) {
    if (attribute == null) {
      return null;
    }
    return attribute.getDisplayName();
  }

  @Override
  public ProcessStepName convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    return ProcessStepName.fromValue(dbData);
  }
}
