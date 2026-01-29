package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;

class RulesetModule implements ExtractionModule {
  private static final PatternMatcher matcher = new PatternMatcher();
  private static final Map<String, ValidatorFunction> VALIDATORS =
      Map.of(
          "is_short_line", (tag, text, i) -> text.length() <= 24,
          "is_not_short_line", (tag, text, i) -> text.length() > 15,
          "is_table", (tag, text, i) -> "table".equals(tag.name()),
          "is_first_line", (tag, text, i) -> i == 0,
          "is_centered", (tag, text, i) -> tag.isCentered());
  private static final Map<String, NormalizerFunction> NORMALIZERS =
      Map.of("date_to_iso", Utils::dateToIso);

  private final RulesetDef ruleset;

  public RulesetModule(RulesetDef ruleset) {
    this.ruleset = ruleset;
  }

  @Override
  public void processTag(TagData tagData, ExtractionContext ctx) {
    String currentSection =
        ctx.getMarkers().isEmpty()
            ? null
            : ctx.getMarkers().get(ctx.getMarkers().size() - 1).sectionName();

    if (ruleset.skipSections() != null
        && currentSection != null
        && ruleset.skipSections().contains(currentSection)) return;

    Pos pos = tagData.tag().pos();
    if (pos == null) throw new IllegalStateException("Tag position is null");

    int tagStartPos = pos.start();
    String tagStr = tagData.tag().outerHtml();

    for (ExtractionRule rule : ruleset.rules()) {
      currentSection =
          ctx.getMarkers().isEmpty()
              ? null
              : ctx.getMarkers().get(ctx.getMarkers().size() - 1).sectionName();

      if (rule.conditions() != null
          && !validate(tagData.tag(), tagData.text(), tagData.index(), rule.conditions())) {
        continue;
      }

      if (rule.skipSections() != null
          && currentSection != null
          && rule.skipSections().contains(currentSection)) {
        continue;
      }

      List<Match> matches =
          matcher.match(rule.patterns(), tagData.text(), rule.greedy(), tagData.tokens());

      int matchStartIdx = 0;
      for (Match match : matches) {
        if (rule.sectionMarkers() != null) {
          for (SectionMarkerDef sm : rule.sectionMarkers()) {
            boolean exists =
                ctx.getMarkers().stream().anyMatch(m -> m.sectionName().equals(sm.label()));
            if (exists) continue;

            ctx.addMarker(
                new SectionMarker(tagData.index(), sm.label(), sm.lineOffset(), sm.maxLines()));

            int offset = tagStr.indexOf(match.text(), 0);
            if (offset != -1) {
              ctx.addExtraction(
                  sm.label() + "_marker",
                  match.text(),
                  tagStartPos + offset,
                  tagStartPos + offset + match.text().length());
            }
          }
        }

        if (rule.extractions() != null) {
          for (ExtractionDef extConfig : rule.extractions()) {
            String matchStr = match.text();
            int actualStart, actualEnd;

            if (extConfig.markTag()) {
              actualStart = tagStartPos;
              actualEnd = tagStartPos + tagStr.length();
            } else {
              int offset = tagStr.indexOf(matchStr, matchStartIdx);
              if (offset == -1) {
                System.out.println(
                    "Warning: matched text not found in tag outer HTML: '"
                        + matchStr
                        + "' in '"
                        + tagStr
                        + "'");
                continue;
              }
              matchStartIdx = offset + matchStr.length();
              actualStart = tagStartPos + offset;
              actualEnd = actualStart + matchStr.length();
            }

            String normalizedText =
                rule.normalizers() != null ? normalize(match.text(), rule.normalizers()) : null;

            ctx.addExtraction(
                extConfig.label(),
                match.text(),
                actualStart,
                actualEnd,
                false,
                null,
                extConfig.priority() != null ? extConfig.priority() : 0,
                extConfig.value() != null ? extConfig.value() : normalizedText);
          }
        }
      }
    }
  }

  @Override
  public void finalize(ExtractionContext ctx) {}

  // ============================================================================
  // Helpers
  // ============================================================================

  private static boolean validate(
      HtmlElement tag, String text, int index, List<String> conditions) {
    if (conditions == null || conditions.isEmpty()) return true;
    for (String name : conditions) {
      ValidatorFunction v = VALIDATORS.get(name);
      if (v == null) throw new IllegalArgumentException("Unknown validator: " + name);
      if (v.validate(tag, text, index)) return true; // OR logic
    }
    return false;
  }

  private static String normalize(String text, List<String> normalizers) {
    if (normalizers == null) return text;
    for (String name : normalizers) {
      NormalizerFunction n = NORMALIZERS.get(name);
      if (n == null) throw new IllegalArgumentException("Unknown normalizer: " + name);
      text = n.normalize(text);
    }
    return text;
  }

  // ============================================================================
  // Instantiation from ruleset file
  // ============================================================================

  public static RulesetModule fromFile(String filename) {
    try {
      ClassPathResource resource =
          new ClassPathResource("rulesets/" + filename, RulesetModule.class);
      String json = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      Gson gson = new Gson();

      @SuppressWarnings("unchecked")
      Map<String, Object> data = gson.fromJson(json, Map.class);
      @SuppressWarnings("unchecked")
      Map<String, Object> vars = (Map<String, Object>) data.remove("vars");

      if (vars != null) data = resolveVars(data, vars);
      RulesetDef ruleset = parseRuleset(gson.toJson(data));
      return new RulesetModule(ruleset);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load " + filename + ": " + e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> T resolveVars(T data, Map<String, Object> vars) {
    if (data instanceof Map) {
      return (T)
          ((Map<String, Object>) data)
              .entrySet().stream()
                  .collect(
                      Collectors.toMap(Map.Entry::getKey, e -> resolveVars(e.getValue(), vars)));
    }
    if (data instanceof List) {
      return (T) ((List<?>) data).stream().map(item -> resolveVars(item, vars)).toList();
    }
    if (data instanceof String && ((String) data).startsWith("$")) {
      String varName = ((String) data).substring(1);
      if (!vars.containsKey(varName))
        throw new IllegalArgumentException("Variable not found: $" + varName);
      return (T) vars.get(varName);
    }
    return data;
  }

  @SuppressWarnings("unchecked")
  private static RulesetDef parseRuleset(String json) {
    Gson gson = new Gson();
    JsonObject obj = gson.fromJson(json, JsonObject.class);

    String name = obj.get("name").getAsString();
    List<String> skipSections =
        obj.has("skip_sections") && !obj.get("skip_sections").isJsonNull()
            ? gson.fromJson(obj.get("skip_sections"), List.class)
            : null;

    List<ExtractionRule> rules = new ArrayList<>();
    for (JsonElement elem : obj.getAsJsonArray("rules")) {
      rules.add(parseRule(elem.getAsJsonObject()));
    }

    return new RulesetDef(name, skipSections, rules);
  }

  @SuppressWarnings("unchecked")
  private static ExtractionRule parseRule(JsonObject obj) {
    String name = obj.has("name") ? obj.get("name").getAsString() : null;

    List<Pattern> patterns = new ArrayList<>();
    for (JsonElement elem : obj.getAsJsonArray("patterns")) {
      if (elem.isJsonPrimitive()) {
        patterns.add(Pattern.ofRegex(elem.getAsString()));
      } else {
        List<TokenConstraint> constraints = new ArrayList<>();
        for (JsonElement c : elem.getAsJsonArray()) {
          constraints.add(parseConstraint(c.getAsJsonObject()));
        }
        patterns.add(Pattern.ofConstraints(constraints));
      }
    }

    String greedy =
        obj.has("greedy") && !obj.get("greedy").isJsonNull()
            ? obj.get("greedy").getAsString()
            : null;
    List<String> conditions =
        obj.has("conditions") && !obj.get("conditions").isJsonNull()
            ? new Gson().fromJson(obj.get("conditions"), List.class)
            : null;
    List<String> normalizers =
        obj.has("normalizers") && !obj.get("normalizers").isJsonNull()
            ? new Gson().fromJson(obj.get("normalizers"), List.class)
            : null;
    List<String> skipSections =
        obj.has("skip_sections") && !obj.get("skip_sections").isJsonNull()
            ? new Gson().fromJson(obj.get("skip_sections"), List.class)
            : null;

    List<ExtractionDef> extractions = null;
    if (obj.has("extractions") && !obj.get("extractions").isJsonNull()) {
      extractions = new ArrayList<>();
      for (JsonElement elem : obj.getAsJsonArray("extractions")) {
        JsonObject eObj = elem.getAsJsonObject();
        extractions.add(
            new ExtractionDef(
                eObj.get("label").getAsString(),
                eObj.has("value") && !eObj.get("value").isJsonNull()
                    ? eObj.get("value").getAsString()
                    : null,
                eObj.has("priority") && !eObj.get("priority").isJsonNull()
                    ? eObj.get("priority").getAsInt()
                    : null,
                eObj.has("mark_tag") && eObj.get("mark_tag").getAsBoolean()));
      }
    }

    List<SectionMarkerDef> sectionMarkers = null;
    if (obj.has("section_markers") && !obj.get("section_markers").isJsonNull()) {
      sectionMarkers = new ArrayList<>();
      for (JsonElement elem : obj.getAsJsonArray("section_markers")) {
        JsonObject sObj = elem.getAsJsonObject();
        sectionMarkers.add(
            new SectionMarkerDef(
                sObj.get("label").getAsString(),
                sObj.has("line_offset") && !sObj.get("line_offset").isJsonNull()
                    ? sObj.get("line_offset").getAsInt()
                    : 0,
                sObj.has("max_lines") && !sObj.get("max_lines").isJsonNull()
                    ? sObj.get("max_lines").getAsInt()
                    : null));
      }
    }

    return new ExtractionRule(
        name, extractions, sectionMarkers, patterns, greedy, conditions, skipSections, normalizers);
  }

  @SuppressWarnings("unchecked")
  private static TokenConstraint parseConstraint(JsonObject obj) {
    Gson gson = new Gson();
    Object text = parseField(obj, "TEXT", gson);
    Object lower = parseField(obj, "LOWER", gson);
    String regex =
        obj.has("REGEX") && !obj.get("REGEX").isJsonNull() ? obj.get("REGEX").getAsString() : null;
    List<String> in =
        obj.has("IN") && !obj.get("IN").isJsonNull()
            ? gson.fromJson(obj.get("IN"), List.class)
            : null;
    List<String> notIn =
        obj.has("NOT_IN") && !obj.get("NOT_IN").isJsonNull()
            ? gson.fromJson(obj.get("NOT_IN"), List.class)
            : null;
    Boolean isDigit = parseBoolean(obj, "IS_DIGIT");
    Boolean isAlpha = parseBoolean(obj, "IS_ALPHA");
    Boolean isTitle = parseBoolean(obj, "IS_TITLE");
    Boolean isUpper = parseBoolean(obj, "IS_UPPER");
    Boolean isSentStart = parseBoolean(obj, "IS_SENT_START");
    String shape =
        obj.has("SHAPE") && !obj.get("SHAPE").isJsonNull() ? obj.get("SHAPE").getAsString() : null;
    String op = obj.has("OP") && !obj.get("OP").isJsonNull() ? obj.get("OP").getAsString() : null;

    return new TokenConstraint(
        text, lower, regex, in, notIn, isDigit, isAlpha, isTitle, isSentStart, shape, op, isUpper);
  }

  private static Object parseField(JsonObject obj, String field, Gson gson) {
    if (!obj.has(field) || obj.get(field).isJsonNull()) return null;
    JsonElement elem = obj.get(field);
    return elem.isJsonPrimitive() ? elem.getAsString() : gson.fromJson(elem, Map.class);
  }

  private static Boolean parseBoolean(JsonObject obj, String field) {
    return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsBoolean() : null;
  }
}
