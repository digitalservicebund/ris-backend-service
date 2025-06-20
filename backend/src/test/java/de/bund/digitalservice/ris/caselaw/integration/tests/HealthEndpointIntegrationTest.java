package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class HealthEndpointIntegrationTest extends BaseIntegrationTest {
  @Autowired MockMvc mockMvc;

  @Test
  void shouldExposeHealthEndpoint() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    mockMvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
  }

  @Test
  void shouldBeUnhealthyWithoutRedis() throws Exception {
    redis.stop();
    mockMvc.perform(get("/actuator/health")).andExpect(status().is5xxServerError());
    mockMvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
    mockMvc.perform(get("/actuator/health/readiness")).andExpect(status().is5xxServerError());
  }
}
