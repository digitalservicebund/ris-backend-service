package de.bund.digitalservice.ris.caselaw.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gravity9.jsonpatch.JsonPatch;
import java.io.IOException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdNodeBasedDeserializer;

/**
 * Custom JSON Deserializer for {@link JsonPatch} as the library does not yet support Jackson 3.
 * This class will use Jackson 2 to deserialize the JsonPatch.
 *
 * @deprecated will be removed once {@link JsonPatch} supports Jackson 3
 */
@Deprecated(since = "2025-12-16")
public class JsonPatchDeserializer extends StdNodeBasedDeserializer<JsonPatch> {
  private final ObjectMapper legacyObjectMapper;

  public JsonPatchDeserializer(ObjectMapper legacyObjectMapper) {
    super(JsonPatch.class);
    this.legacyObjectMapper = legacyObjectMapper;
  }

  @Override
  public JsonPatch convert(JsonNode root, DeserializationContext ctxt) throws JacksonException {
    try (com.fasterxml.jackson.core.JsonParser legacyParser =
        legacyObjectMapper.createParser(root.toString())) {
      return legacyParser.readValueAs(JsonPatch.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
