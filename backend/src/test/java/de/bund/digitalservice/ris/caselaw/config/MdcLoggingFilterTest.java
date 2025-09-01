package de.bund.digitalservice.ris.caselaw.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class MdcLoggingFilterTest {

  private final MdcLoggingFilter filter = new MdcLoggingFilter();
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
    MDC.clear();
  }

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Test
  void shouldSetSessionIdFromCookie() throws Exception {

    request.setCookies(new Cookie("session_id", "abc123"));

    // MDC will be cleared after filter chain, so we need to check inside the chain
    FilterChain spyingChain =
        (req, res) -> {
          assertThat(MDC.get("session_id")).isEqualTo("abc123");
          // Both session_id and request_id are set
          assertThat(MDC.getCopyOfContextMap()).hasSize(2);
        };

    filter.doFilter(request, response, spyingChain);

    assertThat(MDC.getCopyOfContextMap()).isNull();
  }

  @Test
  void shouldGenerateRequestId() throws Exception {

    AtomicReference<String> requestId = new AtomicReference<>();
    // MDC will be cleared after filter chain, so we need to check inside the chain
    FilterChain spyingChain =
        (req, res) -> {
          requestId.set(MDC.get("request_id"));
          assertThat(UUID.fromString(requestId.get())).isInstanceOf(UUID.class);
          assertThat(MDC.getCopyOfContextMap()).hasSize(1);
        };

    filter.doFilter(request, response, spyingChain);

    String headerId = response.getHeader("X-Request-ID");
    assertThat(requestId.get()).isEqualTo(headerId);
    assertThat(MDC.getCopyOfContextMap()).isNull();
  }

  @Test
  void shouldReuseExistingRequestId() throws Exception {

    String requestId = UUID.randomUUID().toString();
    request.addHeader("X-Request-ID", requestId);

    // MDC will be cleared after filter chain, so we need to check inside the chain
    FilterChain spyingChain =
        (req, res) -> {
          assertThat(MDC.get("request_id")).isEqualTo(requestId);
          assertThat(MDC.getCopyOfContextMap()).hasSize(1);
        };

    filter.doFilter(request, response, spyingChain);

    String headerId = response.getHeader("X-Request-ID");
    assertThat(requestId).isEqualTo(headerId);
    assertThat(MDC.getCopyOfContextMap()).isNull();
  }

  @Test
  void shouldExtractDocOfficeWhenLoggedIn() throws Exception {

    OidcUser oidcUser = mock(OidcUser.class);
    when(oidcUser.getAttribute("groups")).thenReturn(List.of("/caselaw/BGH/Intern"));

    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getPrincipal()).thenReturn(oidcUser);

    SecurityContextHolder.getContext().setAuthentication(auth);

    // MDC will be cleared after filter chain, so we need to check inside the chain
    FilterChain spyingChain =
        (req, res) -> {
          assertThat(MDC.get("doc_office_group")).isEqualTo("BGH/Intern");
          // size = 2 -> doc office group + request id
          assertThat(MDC.getCopyOfContextMap()).hasSize(2);
        };

    filter.doFilter(request, response, spyingChain);

    assertThat(MDC.get("doc_office_group")).isNull();
    assertThat(MDC.getCopyOfContextMap()).isNull();
  }

  @Test
  void shouldNotExtractDocOfficeWhenNotAuthenticated() throws Exception {

    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(false);
    SecurityContextHolder.getContext().setAuthentication(auth);

    // MDC will be cleared after filter chain, so we need to check inside the chain
    FilterChain spyingChain =
        (req, res) -> {
          assertThat(MDC.get("doc_office_group")).isNull();
          // size = 1 -> request id is still set
          assertThat(MDC.getCopyOfContextMap()).hasSize(1);
        };

    filter.doFilter(request, response, spyingChain);

    assertThat(MDC.getCopyOfContextMap()).isNull();
  }

  @Test
  void shouldCatchExceptionsAndContinueChain() throws Exception {
    var securityContextMock = mock(SecurityContext.class);
    when(securityContextMock.getAuthentication()).thenThrow(new RuntimeException());
    SecurityContextHolder.setContext(securityContextMock);

    FilterChain spyingChain = spy(MockFilterChain.class);

    assertDoesNotThrow(() -> filter.doFilter(request, response, spyingChain));

    // Verify that the filter chain was continued despite the exception
    verify(spyingChain).doFilter(request, response);
  }
}
