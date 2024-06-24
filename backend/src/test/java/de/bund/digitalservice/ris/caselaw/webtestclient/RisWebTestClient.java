package de.bund.digitalservice.ris.caselaw.webtestclient;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.getMockLogin;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.getMockLoginWithDocOffice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;

public class RisWebTestClient {
  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;

  public RisWebTestClient(MockMvc mockMvc, ObjectMapper objectMapper) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
  }

  public RisRequestSpec withDefaultLogin() {
    return new RisRequestSpec(mockMvc, objectMapper, getMockLogin());
  }

  public RisRequestSpec withLogin(String docOfficeGroup) {
    return new RisRequestSpec(mockMvc, objectMapper, getMockLoginWithDocOffice(docOfficeGroup));
  }
}
