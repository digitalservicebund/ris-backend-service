package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import de.bund.digitalservice.ris.caselaw.domain.docx.Style;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StyleConverter {

  public List<Style> getListFromString(String styles) {
    if (styles == null || styles.isBlank() || !styles.contains(":")) {
      return Collections.emptyList();
    }

    List<Style> styleList = new ArrayList<>();

    if (styles.contains(";")) {
      for (String style : styles.split(";")) {
        Style styleElement = parseStyleParts(style);
        if (styleElement != null) {
          styleList.add(styleElement);
        }
      }
    } else {
      Style styleElement = parseStyleParts(styles);
      if (styleElement != null) {
        styleList.add(styleElement);
      }
    }

    return styleList;
  }

  private Style parseStyleParts(String style) {
    String[] parts = style.split(":");

    if (parts.length != 2) {
      return null;
    }

    return new Style(parts[0].trim(), Collections.singletonList(parts[1].trim()));
  }
}
