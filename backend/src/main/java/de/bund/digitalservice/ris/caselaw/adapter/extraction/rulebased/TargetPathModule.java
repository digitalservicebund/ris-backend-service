package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class TargetPathModule implements ExtractionModule {

  private static final Map<String, String> targetPathMap =
      Map.ofEntries(
          Map.entry("date", "coreData.decisionDate"),
          Map.entry("court", "coreData.court"),
          Map.entry("document_type", "coreData.documentType"),
          Map.entry("file_number", "coreData.fileNumbers.0"), // TODO: handle multiple file numbers
          Map.entry("headline", "shortTexts.headline"),
          Map.entry("guiding_principle", "shortTexts.guidingPrinciple"),
          Map.entry("tenor", "longTexts.tenor"),
          Map.entry("reasons", "longTexts.reasons"),
          Map.entry("case_facts", "longTexts.caseFacts"),
          Map.entry("decision_reasons", "longTexts.decisionReasons"));

  private static final Map<String, String> pdTargetPath =
      Map.ofEntries(
          Map.entry("court", "court"),
          Map.entry("date", "decisionDate"),
          Map.entry("document_type", "documentType"),
          Map.entry("file_number", "fileNumber"));

  @Override
  public void processTag(TagData tagData, ExtractionContext ctx) {}

  @Override
  public void finalize(ExtractionContext ctx) {
    // get top candidates for each extraction class and set target path
    targetPathMap.forEach(
        (className, targetPath) -> {
          List<Extraction> topCandidates = getTopCandidates(ctx.getExtractions(), className, 1);
          topCandidates.forEach(
              extraction -> {
                Extraction updatedExtraction = extraction.withTargetPath(targetPath);
                ctx.updateExtraction(extraction, updatedExtraction);
              });
        });

    // set specific target paths for extractions within previous_decisions section
    List<Extraction> pdExs = getTopCandidates(ctx.getExtractions(), "previous_decisions", 1);
    if (pdExs.isEmpty()) {
      return;
    }
    Extraction pdSection = pdExs.get(0);
    List<Extraction> innerExtractions =
        ctx.getExtractions().stream()
            .filter(
                e ->
                    e.charInterval() != null
                        && pdSection.charInterval() != null
                        && e.charInterval().within(pdSection.charInterval()))
            .collect(Collectors.toList());
    pdTargetPath.forEach(
        (className, targetPath) -> {
          List<Extraction> topCandidates = getTopCandidates(innerExtractions, className, -1);
          for (int i = 0; i < topCandidates.size(); i++) {
            Extraction extraction = topCandidates.get(i);
            String fullTargetPath = String.format("previousDecisions.%d.%s", i, targetPath);
            Extraction updatedExtraction = extraction.withTargetPath(fullTargetPath);
            ctx.updateExtraction(extraction, updatedExtraction);
          }
        });
  }

  private static List<Extraction> getTopCandidates(
      List<Extraction> extractions, String className, int items) {
    return extractions.stream()
        .filter(e -> className.equals(e.extractionClass()))
        .sorted(Comparator.comparingInt((Extraction e) -> e.priority()).reversed())
        .limit(items > 0 ? items : Long.MAX_VALUE)
        .collect(Collectors.toList());
  }
}
