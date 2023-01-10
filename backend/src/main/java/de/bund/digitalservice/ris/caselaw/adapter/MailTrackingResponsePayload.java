package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MailTrackingResponsePayload(String event, List<String> tags) {}
