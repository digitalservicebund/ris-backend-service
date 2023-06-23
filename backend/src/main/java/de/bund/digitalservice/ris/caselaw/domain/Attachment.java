package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record Attachment(String fileName, String fileContent) {}
