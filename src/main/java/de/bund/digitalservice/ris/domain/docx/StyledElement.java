package de.bund.digitalservice.ris.domain.docx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class StyledElement implements DocumentUnitDocx {
  private final Map<String, List<String>> styles = new HashMap<>();

  public void addStyle(String property, String value) {
    if (!styles.containsKey(property)) {
      styles.put(property, new ArrayList<>());
    }
    List<String> values = styles.get(property);
    if (property.equals("text-decoration") && !values.contains(value)) {
      values.add(value);
    } else {
      if (values.isEmpty()){
        values.add(value);
      } else {
        values.set(0, value);
      }
    }
  }

  public void addStyle(Style newStyle) {
    newStyle.value().forEach(value -> addStyle(newStyle.property(), value));
  }

  public Boolean hasStyle() {
    return !styles.isEmpty();
  }

  public String getStyleString() {
    if (styles.isEmpty())
      return "";

    return " style=\""
        + styles.entrySet().stream()
            .map(entry -> entry.getKey() + ": " + String.join(" ", entry.getValue()) + ";")
            .collect(Collectors.joining())
        + "\"";
  }
}
