package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record AttachmentInline(
    UUID id, String format, String name, @PastOrPresent Instant uploadTimestamp) {}
