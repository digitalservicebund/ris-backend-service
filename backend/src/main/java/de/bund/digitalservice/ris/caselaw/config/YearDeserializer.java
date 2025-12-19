package de.bund.digitalservice.ris.caselaw.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.Year;

public class YearDeserializer extends JsonDeserializer<Year> {
  @Override
  public Year deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {
    return Year.parse(jsonParser.getText());
  }
}
