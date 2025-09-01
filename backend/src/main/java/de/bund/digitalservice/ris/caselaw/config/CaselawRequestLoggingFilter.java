package de.bund.digitalservice.ris.caselaw.config;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

public class CaselawRequestLoggingFilter extends CommonsRequestLoggingFilter {
  @Override
  protected boolean shouldLog(@NotNull HttpServletRequest request) {
    return super.shouldLog(request)
        && request.getRequestURI() != null
        // Exclude health checks
        && !request.getRequestURI().startsWith("/actuator");
  }

  @Override
  protected void afterRequest(@NotNull HttpServletRequest request, @NotNull String message) {
    // No op: Do not log a second time after the request
  }
}
