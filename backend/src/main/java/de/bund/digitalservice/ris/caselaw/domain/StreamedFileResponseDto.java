package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public record StreamedFileResponseDto(GetObjectResponse response, StreamingResponseBody body) {}
