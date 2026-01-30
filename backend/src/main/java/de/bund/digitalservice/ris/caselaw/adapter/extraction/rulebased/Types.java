package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// ============================================================================
// Core Data
// ============================================================================

record CharInterval(int startPos, int endPos) {

  public boolean within(CharInterval other) {
    return this.startPos >= other.startPos && this.endPos <= other.endPos;
  }
}

record Extraction(
    String id,
    String extractionClass,
    String extractionText,
    CharInterval charInterval,
    boolean isSection,
    Map<String, Object> attributes,
    int priority,
    String normalizedText,
    String targetPath,
    String annotation) {

  public Extraction {
    attributes = attributes != null ? new HashMap<>(attributes) : null;
  }

  public Extraction(
      String id, String extractionClass, String extractionText, CharInterval charInterval) {
    this(id, extractionClass, extractionText, charInterval, false, null, 0, null, null, null);
  }

  public Extraction withTargetPath(String targetPath) {
    return new Extraction(
        id,
        extractionClass,
        extractionText,
        charInterval,
        isSection,
        attributes,
        priority,
        normalizedText,
        targetPath,
        annotation);
  }
}

// ============================================================================
// Pattern Matching
// ============================================================================

class Token {
  private final String text;
  private final int startChar;
  private final int endChar;
  private boolean isSentStart;

  public Token(String text, int startChar, int endChar) {
    this.text = text;
    this.startChar = startChar;
    this.endChar = endChar;
  }

  public void markSentStart() {
    this.isSentStart = true;
  }

  public String getText() {
    return text;
  }

  public int getStartChar() {
    return startChar;
  }

  public int getEndChar() {
    return endChar;
  }

  public boolean isSentStart() {
    return isSentStart;
  }

  public String lower() {
    return text.toLowerCase();
  }

  public boolean isDigit() {
    return text.matches("\\d+");
  }

  public boolean isAlpha() {
    return text.matches("[a-zA-Z]+");
  }

  public boolean isTitle() {
    // A bit simpler than Python's str.istitle()
    return !text.isEmpty() && Character.isUpperCase(text.charAt(0));
  }

  public boolean isUpper() {
    return !text.equals(text.toLowerCase()) && text.equals(text.toUpperCase());
  }

  public String shape() {
    StringBuilder result = new StringBuilder();
    for (char c : text.toCharArray()) {
      if (Character.isDigit(c)) result.append('d');
      else if (Character.isLowerCase(c)) result.append('x');
      else if (Character.isUpperCase(c)) result.append('X');
      else result.append(c);
    }
    return result.toString();
  }
}

record Match(int start, int end, List<Token> tokens, String text) {
  public int getStartChar() {
    return !tokens.isEmpty() ? tokens.get(0).getStartChar() : 0;
  }

  public int getEndChar() {
    return !tokens.isEmpty() ? tokens.get(tokens.size() - 1).getEndChar() : 0;
  }
}

// ============================================================================
// Ruleset
// ============================================================================

record TokenConstraint(
    Object text,
    Object lower,
    String regex,
    List<String> in,
    List<String> notIn,
    Boolean isDigit,
    Boolean isAlpha,
    Boolean isTitle,
    Boolean isSentStart,
    String shape,
    String op,
    Boolean isUpper) {

  public TokenConstraint(
      Object text,
      Object lower,
      String regex,
      List<String> in,
      List<String> notIn,
      Boolean isDigit,
      Boolean isAlpha,
      Boolean isTitle,
      Boolean isSentStart,
      String shape,
      String op) {
    this(text, lower, regex, in, notIn, isDigit, isAlpha, isTitle, isSentStart, shape, op, null);
  }

  public TokenConstraint(String text) {
    this(text, null, null, null, null, null, null, null, null, null, null, null);
  }
}

record Pattern(List<TokenConstraint> constraints, String regex, boolean isRegex) {
  public static Pattern ofConstraints(List<TokenConstraint> constraints) {
    return new Pattern(constraints, null, false);
  }

  public static Pattern ofRegex(String regex) {
    return new Pattern(null, regex, true);
  }
}

// TODO: use enum for type, greedy, conditions, normalizers

record ExtractionDef(String label, String value, Integer priority, boolean markTag) {}

record SectionMarkerDef(String label, int lineOffset, Integer maxLines) {}

record ExtractionRule(
    String name,
    List<ExtractionDef> extractions,
    List<SectionMarkerDef> sectionMarkers,
    List<Pattern> patterns,
    String greedy,
    List<String> conditions,
    List<String> skipSections,
    List<String> normalizers) {

  public ExtractionRule(String name, List<Pattern> patterns) {
    this(name, null, null, patterns, null, null, null, null);
  }
}

record RulesetDef(String name, List<String> skipSections, List<ExtractionRule> rules) {}

// ============================================================================
// HTML Element Interface
// ============================================================================

record Pos(int start, int end) {}

interface HtmlElement {
  String name();

  Pos pos();

  String innerText();

  String outerHtml();

  boolean isCentered();

  HtmlElement find(String selector, boolean recursive);

  List<HtmlElement> findAll(String selector, boolean recursive);
}

// ============================================================================
// Extraction Context
// ============================================================================

record SectionMarker(
    int lineIdx,
    String sectionName,
    boolean inclusive,
    boolean singleLine,
    Integer lineOffset,
    Integer maxLines) {

  public int startIdx() {
    return lineIdx + (lineOffset != null ? lineOffset : 0);
  }

  public SectionMarker(int lineIdx, String sectionName) {
    this(lineIdx, sectionName, false, false, null, null);
  }

  public SectionMarker(int lineIdx, String sectionName, Integer lineOffset, Integer maxLines) {
    this(lineIdx, sectionName, false, false, lineOffset, maxLines);
  }

  public SectionMarker(int lineIdx, String sectionName, boolean inclusive, boolean singleLine) {
    this(lineIdx, sectionName, inclusive, singleLine, null, null);
  }
}

class ExtractionContext {
  private final List<HtmlElement> childTags;
  private final List<Extraction> extractions;
  private final List<SectionMarker> markers;

  public ExtractionContext(List<HtmlElement> childTags) {
    this(childTags, new ArrayList<>());
  }

  public ExtractionContext(List<HtmlElement> childTags, List<Extraction> extractions) {
    this.childTags = new ArrayList<>(childTags);
    this.extractions = new ArrayList<>(extractions);
    this.markers = new ArrayList<>();
  }

  public Extraction addExtraction(
      String label,
      String text,
      int startPos,
      int endPos,
      boolean isSection,
      Map<String, Object> attrs,
      int priority,
      String normalizedText) {
    Extraction e =
        new Extraction(
            UUID.randomUUID().toString(),
            label,
            text,
            new CharInterval(startPos, endPos),
            isSection,
            attrs,
            priority,
            normalizedText,
            null,
            null);
    extractions.add(e);
    return e;
  }

  public Extraction addExtraction(String label, String text, int startPos, int endPos) {
    return addExtraction(label, text, startPos, endPos, false, null, 0, null);
  }

  public String normalizeWhitespace(String text) {
    return String.join(" ", text.replaceAll("[\u00A0\u2007\u202F]", " ").split("\\s+"));
  }

  public List<HtmlElement> getChildTags() {
    return new ArrayList<>(childTags);
  }

  public List<Extraction> getExtractions() {
    return new ArrayList<>(extractions);
  }

  public List<SectionMarker> getMarkers() {
    return new ArrayList<>(markers);
  }

  public void addMarker(SectionMarker marker) {
    markers.add(marker);
  }

  public void updateExtraction(Extraction oldExtraction, Extraction newExtraction) {
    extractions.remove(oldExtraction);
    extractions.add(newExtraction);
  }
}

record TagData(HtmlElement tag, int index, String text, List<Token> tokens) {
  public int getIndex() {
    return index;
  }

  public HtmlElement getTag() {
    return tag;
  }

  public String getText() {
    return text;
  }

  public List<Token> getTokens() {
    return tokens;
  }
}

// ============================================================================
// Module Interface
// ============================================================================

interface ExtractionModule {
  void processTag(TagData tagData, ExtractionContext ctx);

  void finalize(ExtractionContext ctx);
}

// ============================================================================
// Validator and Noramlizer Interfaces
// ============================================================================

@FunctionalInterface
interface ValidatorFunction {
  boolean validate(HtmlElement tag, String text, Integer lineIndex);
}

@FunctionalInterface
interface NormalizerFunction {
  String normalize(String text);
}
