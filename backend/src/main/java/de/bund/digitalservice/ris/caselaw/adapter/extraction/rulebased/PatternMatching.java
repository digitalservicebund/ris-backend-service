package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Tokenizer {
  private static final Set<String> SENT_END_CHARS = Set.of(".", "!", "?", "…");

  public List<Token> tokenize(String text) {
    List<Token> tokens = new ArrayList<>();
    String patternStr =
        "(\\d+\\.\\d+\\.\\d+|[a-zA-ZäöüßÄÖÜ]+(?:-[a-zA-ZäöüßÄÖÜ]+)+|"
            + "[a-zA-ZäöüßÄÖÜ]+(?:-\\d+)+|\\d+(?:-[a-zA-ZäöüßÄÖÜ]+)+|"
            + "\\d+\\.|\\w+|[^\\w\\s])";

    java.util.regex.Pattern pattern =
        java.util.regex.Pattern.compile(
            patternStr, java.util.regex.Pattern.UNICODE_CHARACTER_CLASS);
    java.util.regex.Matcher matcher = pattern.matcher(text);

    while (matcher.find()) {
      tokens.add(new Token(matcher.group(), matcher.start(), matcher.end()));
    }

    if (!tokens.isEmpty()) {
      tokens.get(0).markSentStart();
      for (int i = 1; i < tokens.size(); i++) {
        if (SENT_END_CHARS.contains(tokens.get(i - 1).getText())) {
          tokens.get(i).markSentStart();
        }
      }
    }
    return tokens;
  }
}

class PatternMatcher {
  private final Tokenizer tokenizer = new Tokenizer();

  public List<Match> match(List<Pattern> patterns, String text, String greedy, List<Token> tokens) {
    if (tokens == null) tokens = tokenizer.tokenize(text);

    List<Match> matches = new ArrayList<>();
    for (Pattern pattern : patterns) {
      matches.addAll(matchPattern(pattern, text, tokens));
    }

    if (greedy != null && matches.size() > 1) {
      matches = "LONGEST".equals(greedy) ? filterLongest(matches) : filterFirst(matches);
    }
    return matches;
  }

  public List<Match> match(List<Pattern> patterns, String text) {
    return match(patterns, text, null, null);
  }

  private List<Match> matchPattern(Pattern pattern, String fullText, List<Token> tokens) {
    if (pattern.isRegex()) return matchRegex(pattern.regex(), tokens, fullText);

    List<Match> matches = new ArrayList<>();
    for (int i = 0; i < tokens.size(); i++) {
      Integer matchEnd = tryMatchAt(pattern.constraints(), tokens, i);
      if (matchEnd != null) {
        String text =
            fullText.substring(tokens.get(i).getStartChar(), tokens.get(matchEnd - 1).getEndChar());
        matches.add(new Match(i, matchEnd, tokens.subList(i, matchEnd), text));
      }
    }
    return matches;
  }

  private List<Match> matchRegex(String regexPattern, List<Token> tokens, String fullText) {
    List<Match> matches = new ArrayList<>();
    java.util.regex.Pattern pattern =
        java.util.regex.Pattern.compile(
            regexPattern, java.util.regex.Pattern.UNICODE_CHARACTER_CLASS);
    java.util.regex.Matcher matcher = pattern.matcher(fullText);

    while (matcher.find()) {
      int groupNum = (matcher.groupCount() >= 1) ? 1 : 0;
      int startChar = matcher.start(groupNum);
      int endChar = matcher.end(groupNum);

      List<Token> matchingTokens = new ArrayList<>();
      Integer startIdx = null, endIdx = null;

      for (int i = 0; i < tokens.size(); i++) {
        Token token = tokens.get(i);
        if (token.getStartChar() < endChar && token.getEndChar() > startChar) {
          if (startIdx == null) startIdx = i;
          endIdx = i + 1;
          matchingTokens.add(token);
        }
      }

      if (!matchingTokens.isEmpty() && startIdx != null) {
        matches.add(new Match(startIdx, endIdx, matchingTokens, matcher.group(groupNum)));
      }
    }
    return matches;
  }

  private Integer tryMatchAt(List<TokenConstraint> pattern, List<Token> tokens, int startIdx) {
    int tokenIdx = startIdx, patternIdx = 0;

    while (patternIdx < pattern.size()) {
      if (tokenIdx >= tokens.size()) {
        return pattern.subList(patternIdx, pattern.size()).stream()
                .allMatch(p -> "?".equals(p.op()) || "*".equals(p.op()))
            ? tokenIdx
            : null;
      }

      TokenConstraint c = pattern.get(patternIdx);
      String op = c.op();

      if ("?".equals(op)) {
        if (tokenMatches(tokens.get(tokenIdx), c)) tokenIdx++;
        patternIdx++;
      } else if ("*".equals(op)) {
        while (tokenIdx < tokens.size() && tokenMatches(tokens.get(tokenIdx), c)) tokenIdx++;
        patternIdx++;
      } else if ("+".equals(op)) {
        if (!tokenMatches(tokens.get(tokenIdx), c)) return null;
        tokenIdx++;
        while (tokenIdx < tokens.size() && tokenMatches(tokens.get(tokenIdx), c)) tokenIdx++;
        patternIdx++;
      } else if ("!".equals(op)) {
        if (tokenMatches(tokens.get(tokenIdx), c)) return null;
        patternIdx++;
      } else {
        if (!tokenMatches(tokens.get(tokenIdx), c)) return null;
        tokenIdx++;
        patternIdx++;
      }
    }
    return tokenIdx;
  }

  @SuppressWarnings("unchecked")
  private boolean tokenMatches(Token token, TokenConstraint c) {
    if (c.text() != null) {
      if (c.text() instanceof String && !token.getText().equals(c.text())) return false;
      if (c.text() instanceof Map) {
        Map<String, Object> map = (Map<String, Object>) c.text();
        if (map.containsKey("REGEX") && !token.getText().matches((String) map.get("REGEX")))
          return false;
        if (map.containsKey("IN") && !((List<String>) map.get("IN")).contains(token.getText()))
          return false;
        if (map.containsKey("NOT_IN")
            && ((List<String>) map.get("NOT_IN")).contains(token.getText())) return false;
      }
    }

    if (c.lower() != null) {
      if (c.lower() instanceof String && !token.lower().equals(c.lower())) return false;
      if (c.lower() instanceof Map) {
        Map<String, Object> map = (Map<String, Object>) c.lower();
        if (map.containsKey("REGEX") && !token.lower().matches((String) map.get("REGEX")))
          return false;
        if (map.containsKey("IN") && !((List<String>) map.get("IN")).contains(token.lower()))
          return false;
        if (map.containsKey("NOT_IN") && ((List<String>) map.get("NOT_IN")).contains(token.lower()))
          return false;
      }
    }

    if (c.regex() != null && !token.getText().matches(c.regex())) return false;
    if (c.in() != null && !c.in().contains(token.getText())) return false;
    if (c.notIn() != null && c.notIn().contains(token.getText())) return false;
    if (c.isDigit() != null && token.isDigit() != c.isDigit()) return false;
    if (c.isAlpha() != null && token.isAlpha() != c.isAlpha()) return false;
    if (c.isTitle() != null && token.isTitle() != c.isTitle()) return false;
    if (c.shape() != null && !token.shape().equals(c.shape())) return false;
    if (c.isSentStart() != null && token.isSentStart() != c.isSentStart()) return false;

    return true;
  }

  private List<Match> filterLongest(List<Match> matches) {
    matches.sort(
        Comparator.comparingInt((Match m) -> -(m.end() - m.start()))
            .thenComparingInt(Match::start));
    List<Match> result = new ArrayList<>();
    Set<Integer> used = new HashSet<>();

    for (Match m : matches) {
      boolean overlap = false;
      for (int i = m.start(); i < m.end(); i++) {
        if (used.contains(i)) {
          overlap = true;
          break;
        }
      }
      if (!overlap) {
        result.add(m);
        for (int i = m.start(); i < m.end(); i++) used.add(i);
      }
    }
    result.sort(Comparator.comparingInt(Match::start));
    return result;
  }

  private List<Match> filterFirst(List<Match> matches) {
    matches.sort(Comparator.comparingInt(Match::start));
    List<Match> result = new ArrayList<>();
    int lastEnd = -1;
    for (Match m : matches) {
      if (m.start() >= lastEnd) {
        result.add(m);
        lastEnd = m.end();
      }
    }
    return result;
  }
}
