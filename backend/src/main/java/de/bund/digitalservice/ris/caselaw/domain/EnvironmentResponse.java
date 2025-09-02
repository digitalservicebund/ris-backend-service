package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

/**
 * Represents the information about the current environment and the corresponding url to the
 * NeuRIS-Portal.
 *
 * @param environment the current environment the user is logged into
 * @param portalUrl the url to the NeuRIS-Portal, corresponding to the current environment
 */
@Builder
public record EnvironmentResponse(String environment, String portalUrl) {}
