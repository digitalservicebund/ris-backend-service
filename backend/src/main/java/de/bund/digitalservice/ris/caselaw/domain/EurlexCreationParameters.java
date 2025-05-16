package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;

public record EurlexCreationParameters(
    DocumentationOffice documentationOffice, List<String> celexNumbers) {}
