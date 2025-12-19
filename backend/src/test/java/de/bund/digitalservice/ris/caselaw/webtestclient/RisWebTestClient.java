package de.bund.digitalservice.ris.caselaw.webtestclient;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.getMockLogin;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.getMockLoginExternal;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.getMockLoginWithDocOffice;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.org.checkerframework.checker.nullness.qual.Nullable;

public class RisWebTestClient {
  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final Cookie csrfCookie;

  public RisWebTestClient(MockMvc mockMvc, ObjectMapper objectMapper) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;

    try {
      MockHttpServletRequestBuilder csrfRequestBuilder = MockMvcRequestBuilders.get("/csrf");
      MvcResult mvcResult = this.mockMvc.perform(csrfRequestBuilder).andReturn();
      csrfCookie = mvcResult.getResponse().getCookie("XSRF-TOKEN");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Performs a request with a default internal login, generating a random user ID.
   *
   * @return A RisRequestSpec for building the request.
   */
  public RisRequestSpec withDefaultLogin() {
    return new RisRequestSpec(
        mockMvc, objectMapper, getMockLogin(null), csrfCookie); // Pass null to generate random ID
  }

  /**
   * Performs a request with a default internal login, using a specified user ID.
   *
   * @param userId The UUID to use for the mocked user's 'sub' claim.
   * @return A RisRequestSpec for building the request.
   */
  public RisRequestSpec withDefaultLogin(@Nullable UUID userId) {
    return new RisRequestSpec(mockMvc, objectMapper, getMockLogin(userId), csrfCookie);
  }

  public RisRequestSpec withExternalLogin() {
    return new RisRequestSpec(mockMvc, objectMapper, getMockLoginExternal(null), csrfCookie);
  }

  public RisRequestSpec withLogin(String docOfficeGroup) {
    return withLogin(docOfficeGroup, "Internal");
  }

  public RisRequestSpec withLogin(String docOfficeGroup, String role) {
    return new RisRequestSpec(
        mockMvc, objectMapper, getMockLoginWithDocOffice(docOfficeGroup, role, null), csrfCookie);
  }

  public RisRequestSpec withoutAuthentication() {
    return new RisRequestSpec(mockMvc, objectMapper, null, csrfCookie);
  }
}
