package de.bund.digitalservice.ris.caselaw.adapter.publication;

public record ManualPortalPublicationResult(
    RelatedPendingProceedingPublicationResult relatedPendingProceedingsPublicationResult) {

  public enum RelatedPendingProceedingPublicationResult {
    /**
     * If all previously unresolved pending proceedings that are linked with a decision are
     * successfully published as resolved automatically.
     */
    SUCCESS,

    /** There were no unresolved linked pending proceedings to be published. */
    NO_ACTION,

    /**
     * For any of the linked unresolved pending proceedings, resolving and/or publishing them has
     * failed.
     */
    ERROR,
  }
}
