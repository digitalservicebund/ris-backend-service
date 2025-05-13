package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import java.util.List;

public record EurlexCreationParameters(DocumentationOffice documentationOffice, List<String> celexNumbers) {}
