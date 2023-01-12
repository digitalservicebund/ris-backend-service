package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MailTrackingResponsePayload(@NotNull String event, @NotEmpty List<String> tags) {}
