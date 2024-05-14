package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import java.util.UUID;
import lombok.Builder;

/**
 * A record representing a region from the region data table.
 *
 * @param id id of the region
 * @param code unique code of the region (2-3 characters)
 * @param longText name of the region
 */
@Builder
public record Region(UUID id, String code, String longText) {}
