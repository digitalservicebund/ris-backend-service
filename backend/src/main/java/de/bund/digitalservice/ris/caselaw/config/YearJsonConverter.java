package de.bund.digitalservice.ris.caselaw.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.YearDeserializer;
import java.time.Year;
import lombok.experimental.UtilityClass;

@UtilityClass
public class YearJsonConverter {

  public static SimpleModule yearJsonConverter() {
    SimpleModule yearModule = new SimpleModule();
    yearModule.addSerializer(Year.class, new YearSerializer());
    yearModule.addDeserializer(Year.class, new YearDeserializer());
    return yearModule;
  }
}
