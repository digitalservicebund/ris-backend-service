package de.bund.digitalservice.ris.caselaw.domain;

import java.util.Optional;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder(toBuilder = true)
public record Source(SourceValue value, String sourceRawValue, Reference reference) {
  @NotNull
  @Override
  public String toString() {
    return Optional.ofNullable(value).map(SourceValue::getLabel).orElse(sourceRawValue);
  }
}
