package de.bund.digitalservice.ris.caselaw.domain.textcheck;

import java.util.List;

public record Suggestion(String word, List<Match> matches) {}
