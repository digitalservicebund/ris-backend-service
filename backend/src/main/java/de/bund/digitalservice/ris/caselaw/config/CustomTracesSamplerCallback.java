package de.bund.digitalservice.ris.caselaw.config;

import io.sentry.SamplingContext;
import io.sentry.SentryOptions.TracesSamplerCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomTracesSamplerCallback implements TracesSamplerCallback {
  @Override
  public @Nullable Double sample(@NotNull SamplingContext context) {
    if (context.getCustomSamplingContext() == null) {
      return null;
    }

    ServerHttpRequest request =
        (ServerHttpRequest) context.getCustomSamplingContext().get("request");

    if (request == null) {
      return null;
    }

    String url = request.getPath().value();

    if (url.startsWith("/actuator")) {
      return 0d;
    } else {
      return null;
    }
  }
}
