package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class SectionBuilderModule implements ExtractionModule {
  @Override
  public void processTag(TagData tagData, ExtractionContext ctx) {}

  @Override
  public void finalize(ExtractionContext ctx) {
    List<SectionMarker> markers = ctx.getMarkers();

    for (int i = 0; i < markers.size(); i++) {
      SectionMarker marker = markers.get(i);
      List<HtmlElement> sectionTags = getSectionTags(ctx, i);

      if (sectionTags.isEmpty()) continue;

      HtmlElement firstTag = sectionTags.get(0);
      HtmlElement lastTag = sectionTags.get(sectionTags.size() - 1);
      if (firstTag.pos() == null || lastTag.pos() == null) continue;

      String sectionHtml =
          sectionTags.stream().map(HtmlElement::outerHtml).collect(Collectors.joining());
      List<Integer> rowStartIdxs =
          sectionTags.stream()
              .map(t -> t.pos() != null ? t.pos().start() : null)
              .collect(Collectors.toList());

      ctx.addExtraction(
          marker.sectionName(),
          sectionHtml,
          firstTag.pos().start(),
          lastTag.pos().end(),
          true,
          Map.of("row_start_idxs", rowStartIdxs),
          0,
          null);
    }
  }

  private List<HtmlElement> getSectionTags(ExtractionContext ctx, int idx) {
    SectionMarker marker = ctx.getMarkers().get(idx);
    int startIdx = marker.startIdx();
    int endIdx = ctx.getChildTags().size();

    for (int i = idx + 1; i < ctx.getMarkers().size(); i++) {
      SectionMarker nextMarker = ctx.getMarkers().get(i);

      if (nextMarker.startIdx() == marker.startIdx()) continue;

      endIdx =
          (nextMarker.lineIdx() == marker.lineIdx()) ? nextMarker.startIdx() : nextMarker.lineIdx();
      break;
    }

    if (marker.maxLines() != null) {
      endIdx = Math.min(endIdx, startIdx + marker.maxLines());
    }

    if (startIdx >= endIdx || startIdx >= ctx.getChildTags().size()) return new ArrayList<>();

    List<HtmlElement> sectionTags =
        new ArrayList<>(
            ctx.getChildTags().subList(startIdx, Math.min(endIdx, ctx.getChildTags().size())));
    return trimEmptyTags(sectionTags);
  }

  private List<HtmlElement> trimEmptyTags(List<HtmlElement> tags) {
    while (!tags.isEmpty() && isTagEmpty(tags.get(0))) {
      tags.remove(0);
    }
    while (!tags.isEmpty() && isTagEmpty(tags.get(tags.size() - 1))) {
      tags.remove(tags.size() - 1);
    }
    return tags;
  }

  private boolean isTagEmpty(HtmlElement tag) {
    return tag.innerText().strip().isEmpty();
  }
}
