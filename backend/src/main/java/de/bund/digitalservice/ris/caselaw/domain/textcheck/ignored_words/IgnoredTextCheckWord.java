package de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words;

import java.util.UUID;

public record IgnoredTextCheckWord(UUID id, IgnoredTextCheckType type, Boolean isEditable) {}
