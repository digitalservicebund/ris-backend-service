package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import lombok.Builder;

/**
 * Represents the result of a LDML transformation operation.
 *
 * @param ldml the documentation unit LDML representation
 * @param success whether the transformation was successful
 * @param statusMessages a list of issues found during the transformation
 */
@Builder
public record LdmlTransformationResult(String ldml, boolean success, List<String> statusMessages) {}
