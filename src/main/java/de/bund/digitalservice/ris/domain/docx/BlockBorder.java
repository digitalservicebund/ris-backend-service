package de.bund.digitalservice.ris.domain.docx;

import java.util.HashMap;
import java.util.Map;

public class BlockBorder {
  private Map<Position, Border> borders = new HashMap<>();

  public BlockBorder() {
    borders.put(Position.TOP, new Border());
    borders.put(Position.RIGHT, new Border());
    borders.put(Position.BOTTOM, new Border());
    borders.put(Position.LEFT, new Border());
  }

  public void setColor(Position position, String color) {
    borders.get(position).color = color;
  }
  public void setColor(String color) {
    setColor(Position.TOP, color);
    setColor(Position.RIGHT, color);
    setColor(Position.BOTTOM, color);
    setColor(Position.LEFT, color);
  }

  public void setWidth(Position position, Integer width) {
    borders.get(position).width = width;
  }
  public void setWidth(Integer width) {
    setWidth(Position.TOP, width);
    setWidth(Position.RIGHT, width);
    setWidth(Position.BOTTOM, width);
    setWidth(Position.LEFT, width);
  }

  public void setType(Position position, String type) {
    borders.get(position).type = type;
  }

  public void setType(String type) {
    setType(Position.TOP, type);
    setType(Position.RIGHT, type);
    setType(Position.BOTTOM, type);
    setType(Position.LEFT, type);
  }

  private class Border {
    protected String color;
    protected Integer width;
    protected String type;
  }

  public enum Position {
    TOP,
    RIGHT,
    BOTTOM,
    LEFT
  }
}
