package de.bund.digitalservice.ris.caselaw.domain.textcheck;

import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record Match(
    String word,
    String message,
    String shortMessage,
    CategoryType category,
    List<Replacement> replacements,
    int offset,
    int length,
    Context context,
    String sentence,
    Type type,
    Rule rule,
    boolean ignoreForIncompleteSentence,
    int contextForSureMatch) {}
