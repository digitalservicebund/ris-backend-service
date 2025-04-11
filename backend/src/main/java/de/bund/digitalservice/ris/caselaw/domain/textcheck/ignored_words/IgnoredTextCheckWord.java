package de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words;

import java.util.UUID;

/**
 * Represents a word that is ignored during text checks.
 *
 * @param id the id
 * @param type if the word is ignored on a global or documentation_unit level
 * @param isEditable if the ignore status can be changed (not possible for global jDV ignore words)
 * @param word the word to be ignored
 */
public record IgnoredTextCheckWord(
    UUID id, IgnoredTextCheckType type, Boolean isEditable, String word) {}
