package de.bund.digitalservice.ris.caselaw.domain.textcheck;

import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record Match(
    Integer id,
    String word,
    String message,
    String shortMessage,
    CategoryType category,
    List<Replacement> replacements,
    int offset,
    Integer htmlOffset,
    int length,
    Context context,
    String sentence,
    Type type,
    Rule rule,
    boolean ignoreForIncompleteSentence,
    int contextForSureMatch) {}
