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
          "is_short_line", (tag, text) -> text.length() <= 24,
          "is_not_short_line", (tag, text) -> text.length() > 15,
          "is_table", (tag, text) -> "table".equals(tag.name()));
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
      if (rule.conditions() != null && !validate(tagData.tag(), tagData.text(), rule.conditions()))
        continue;

      List<Match> matches =
          matcher.match(rule.patterns(), tagData.text(), rule.greedy(), tagData.tokens());

      for (Match match : matches) {

        // System.out.println("Matched rule " + rule.label() + ": " + match.text());

        String label = rule.label();

        if ("section_marker".equals(rule.type())) {
          String secLabel = rule.label().replaceAll("[0-9]+$", "");
          label = secLabel + "_marker";

          boolean exists =
              ctx.getMarkers().stream().anyMatch(m -> m.sectionName().equals(secLabel));
          if (exists) continue;

          ctx.addMarker(
              new SectionMarker(tagData.index(), secLabel, rule.inclusive(), rule.singleLine()));
        }

        int actualStart, actualEnd;
        if (rule.markTag()) {
          actualStart = tagStartPos;
          actualEnd = tagStartPos + tagStr.length();
        } else {
          int offset = tagStr.indexOf(match.text());
          if (offset == -1) {
            System.out.println(
                "Warning: matched text not found in tag outer HTML: '"
                    + match.text()
                    + "' in '"
                    + tagStr
                    + "'");
            continue;
          }
          actualStart = tagStartPos + offset;
          actualEnd = actualStart + match.text().length();
        }

        // normalized_text = self._normalize(match.text, rule.normalizers) if rule.normalizers else
        // None

        String normalizedText =
            rule.normalizers() != null ? normalize(match.text(), rule.normalizers()) : null;

        ctx.addExtraction(
            label,
            match.text(),
            actualStart,
            actualEnd,
            false,
            null,
            rule.getPriority(),
            normalizedText);

        if (rule.followedBySection() != null) {
          ctx.addMarker(
              new SectionMarker(tagData.index() + 1, rule.followedBySection(), true, false));
        }
      }
    }
  }

  @Override
  public void finalize(ExtractionContext ctx) {}

  // ============================================================================
  // Helpers
  // ============================================================================

  private static boolean validate(HtmlElement tag, String text, List<String> conditions) {
    if (conditions == null) return true;
    for (String name : conditions) {
      ValidatorFunction v = VALIDATORS.get(name);
      if (v == null) throw new IllegalArgumentException("Unknown validator: " + name);
      if (!v.validate(tag, text)) return false;
    }
    return true;
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
    String label = obj.get("label").getAsString();
    String type = obj.has("type") ? obj.get("type").getAsString() : "extraction";

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
    Integer priority =
        obj.has("priority") && !obj.get("priority").isJsonNull()
            ? obj.get("priority").getAsInt()
            : null;
    boolean markTag = obj.has("mark_tag") && obj.get("mark_tag").getAsBoolean();
    String followedBy =
        obj.has("followed_by_section") && !obj.get("followed_by_section").isJsonNull()
            ? obj.get("followed_by_section").getAsString()
            : null;
    boolean inclusive = obj.has("inclusive") && obj.get("inclusive").getAsBoolean();
    boolean singleLine = obj.has("single_line") && obj.get("single_line").getAsBoolean();

    return new ExtractionRule(
        label,
        type,
        patterns,
        greedy,
        conditions,
        normalizers,
        priority,
        markTag,
        followedBy,
        inclusive,
        singleLine);
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
    Boolean isSentStart = parseBoolean(obj, "IS_SENT_START");
    String shape =
        obj.has("SHAPE") && !obj.get("SHAPE").isJsonNull() ? obj.get("SHAPE").getAsString() : null;
    String op = obj.has("OP") && !obj.get("OP").isJsonNull() ? obj.get("OP").getAsString() : null;

    return new TokenConstraint(
        text, lower, regex, in, notIn, isDigit, isAlpha, isTitle, isSentStart, shape, op);
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
