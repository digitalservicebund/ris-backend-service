package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record MailAttachmentImage(String fileName, byte[] fileContent) {}
