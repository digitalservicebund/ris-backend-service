package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;
import lombok.Builder;

@Builder
public record Attachment(
    String s3path,
    String format,
    String name,
    @PastOrPresent Instant uploadTimestamp,
    AttachmentType type) {}
