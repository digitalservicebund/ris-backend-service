package de.bund.digitalservice.ris.caselaw.adapter;

import java.util.List;

public record PortalPublicationResult(List<String> changedPaths, List<String> deletedPaths) {}
