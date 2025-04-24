package de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words;

import java.util.UUID;

/**
 * Represents a word that is ignored during text checks.
 *
 * @param id the id
 * @param type if the word is ignored on a global or documentation_unit level
 * @param word the word to be ignored
 */
public record IgnoredTextCheckWord(UUID id, IgnoredTextCheckType type, String word) {}
