package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class SectionBuilderModule implements ExtractionModule {
  @Override
  public void processTag(TagData tagData, ExtractionContext ctx) {}

  @Override
  public void finalize(ExtractionContext ctx) {
    List<SectionMarker> markers =
        ctx.getMarkers().stream().sorted(Comparator.comparingInt(SectionMarker::lineIdx)).toList();

    for (int i = 0; i < markers.size(); i++) {
      SectionMarker current = markers.get(i);
      SectionMarker next = (i + 1 < markers.size()) ? markers.get(i + 1) : null;

      int startIdx = current.inclusive() ? current.lineIdx() : current.lineIdx() + 1;
      int endIdx =
          current.singleLine()
              ? current.lineIdx() + 1
              : (next != null ? next.lineIdx() : ctx.getChildTags().size());

      List<HtmlElement> tags = new ArrayList<>(ctx.getChildTags().subList(startIdx, endIdx));

      while (!tags.isEmpty() && tags.get(0).innerText().strip().isEmpty()) tags.remove(0);
      while (!tags.isEmpty() && tags.get(tags.size() - 1).innerText().strip().isEmpty())
        tags.remove(tags.size() - 1);

      if (tags.isEmpty()) continue;

      String html = tags.stream().map(HtmlElement::outerHtml).collect(Collectors.joining());
      Pos first = tags.get(0).pos(), last = tags.get(tags.size() - 1).pos();
      if (first == null || last == null) continue;

      List<Integer> rowStarts =
          tags.stream().map(t -> t.pos() != null ? t.pos().start() : null).toList();
      Map<String, Object> attrs = Map.of("row_start_idxs", rowStarts);

      ctx.addExtraction(
          current.sectionName(), html, first.start(), last.end(), true, attrs, 0, null);
    }
  }
}
