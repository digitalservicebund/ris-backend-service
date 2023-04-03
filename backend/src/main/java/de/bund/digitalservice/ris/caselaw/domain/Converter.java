package de.bund.digitalservice.ris.caselaw.domain;

public interface Converter<T> {
  T convert(Object part);
}
