package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record MailAttachment(String fileName, String fileContent) {}
