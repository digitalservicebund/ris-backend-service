package de.bund.digitalservice.ris.caselaw;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gravity9.jsonpatch.JsonPatch;
import de.bund.digitalservice.ris.caselaw.config.JsonPatchDeserializer;
import de.bund.digitalservice.ris.caselaw.config.JsonPatchSerializer;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@TestConfiguration
public class TestConfig {
  @Bean
  public RisWebTestClient risWebTestClient(MockMvc mockMvc) {
    var legacyObjectMapper = new ObjectMapper();
    legacyObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    legacyObjectMapper.registerModule(new JavaTimeModule());

    var module = new SimpleModule();
    module.addDeserializer(JsonPatch.class, new JsonPatchDeserializer(legacyObjectMapper));
    module.addSerializer(JsonPatch.class, new JsonPatchSerializer(legacyObjectMapper));

    return new RisWebTestClient(mockMvc, JsonMapper.builder().addModule(module).build());
  }
}
