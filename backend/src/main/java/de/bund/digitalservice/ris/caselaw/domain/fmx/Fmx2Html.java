package de.bund.digitalservice.ris.caselaw.domain.fmx;

import de.bund.digitalservice.ris.caselaw.domain.Attachment2Html;

/**
 * Object with the content of the fmx document converted to html
 *
 * @param html - converted html of the fmx document
 */
public record Fmx2Html(String html) implements Attachment2Html {
  public static final Fmx2Html EMPTY = new Fmx2Html(null);
}
