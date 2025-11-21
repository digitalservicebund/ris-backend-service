package de.bund.digitalservice.ris.caselaw;

import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@TestConfiguration
public class TestConfig {
  @Bean
  public RisWebTestClient risWebTestClient(MockMvc mockMvc) {
    /*    var objectMapper = new ObjectMapper();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.registerModule(new JavaTimeModule());*/

    return new RisWebTestClient(mockMvc, JsonMapper.builder().build());
  }
}
