package de.bund.digitalservice.ris.domain.docx;

import java.util.EnumMap;
import java.util.Map;

public class BlockBorder {
  private final Map<Position, Border> borders = new EnumMap<>(Position.class);

  private void setColor(Position position, String color) {
    borders.computeIfAbsent(position, key -> new Border());
    borders.get(position).color = color;
  }

  private void setWidth(Position position, Integer width) {
    borders.computeIfAbsent(position, key -> new Border());
    borders.get(position).width = width;
  }

  private void setType(Position position, String type) {
    borders.computeIfAbsent(position, key -> new Border());
    borders.get(position).type = type;
  }

  public void setTop(String color, Integer width, String type) {
    setColor(Position.TOP, color);
    setWidth(Position.TOP, width);
    setType(Position.TOP, type);
  }

  public void setRight(String color, Integer width, String type) {
    setColor(Position.RIGHT, color);
    setWidth(Position.RIGHT, width);
    setType(Position.RIGHT, type);
  }

  public void setBottom(String color, Integer width, String type) {
    setColor(Position.BOTTOM, color);
    setWidth(Position.BOTTOM, width);
    setType(Position.BOTTOM, type);
  }

  public void setLeft(String color, Integer width, String type) {
    setColor(Position.LEFT, color);
    setWidth(Position.LEFT, width);
    setType(Position.LEFT, type);
  }

  public void removeTop() {
    borders.remove(Position.TOP);
  }

  public void removeBottom() {
    borders.remove(Position.BOTTOM);
  }

  public Boolean isSet() {
    return borders.size() > 0;
  }

  private String positionToHtmlString(Position position) {
    var border = borders.get(position);

    return "border-"
        + position.name().toLowerCase()
        + ": "
        + border.width
        + "px "
        + border.type
        + " "
        + border.color
        + ";";
  }

  public String toHtmlString() {
    var sb = new StringBuilder();

    if (borders.containsKey(Position.TOP)) sb.append(positionToHtmlString(Position.TOP));
    if (borders.containsKey(Position.RIGHT)) sb.append(positionToHtmlString(Position.RIGHT));
    if (borders.containsKey(Position.BOTTOM)) sb.append(positionToHtmlString(Position.BOTTOM));
    if (borders.containsKey(Position.LEFT)) sb.append(positionToHtmlString(Position.LEFT));

    return sb.toString();
  }

  private static class Border {
    protected String color;
    protected Integer width;
    protected String type;
  }

  private enum Position {
    TOP,
    RIGHT,
    BOTTOM,
    LEFT
  }
}
