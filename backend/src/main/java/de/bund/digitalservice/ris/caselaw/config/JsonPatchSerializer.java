package de.bund.digitalservice.ris.caselaw.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gravity9.jsonpatch.JsonPatch;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Custom JSON Serializer for {@link JsonPatch} as the library does not yet support Jackson 3. This
 * class will use Jackson 2 to serialize the JsonPatch.
 */
@Deprecated(since = "2025-12-16")
public class JsonPatchSerializer extends StdSerializer<JsonPatch> {
  private final ObjectMapper legacyObjectMapper;

  public JsonPatchSerializer(ObjectMapper legacyObjectMapper) {
    super(JsonPatch.class);
    this.legacyObjectMapper = legacyObjectMapper;
  }

  @Override
  public void serialize(JsonPatch value, JsonGenerator gen, SerializationContext provider)
      throws JacksonException {
    try {
      gen.writeRawValue(legacyObjectMapper.writerFor(JsonPatch.class).writeValueAsString(value));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
