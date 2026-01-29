package de.bund.digitalservice.ris.caselaw.adapter.extraction.rulebased;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PatternMatcherTest {

  @Test
  void testTokenProperties() {
    // Test token property accessors
    Token token = new Token("Hello", 0, 5);
    assertThat(token.lower()).isEqualTo("hello");
    assertThat(token.isTitle()).isTrue();
    assertThat(token.isAlpha()).isTrue();
    assertThat(token.isDigit()).isFalse();
    assertThat(token.shape()).isEqualTo("Xxxxx");

    Token token2 = new Token("123", 0, 3);
    assertThat(token2.isDigit()).isTrue();
    assertThat(token2.shape()).isEqualTo("ddd");
  }

  @Test
  void testTokenizerBasic() {
    // Test basic tokenization
    Tokenizer tokenizer = new Tokenizer();
    List<Token> tokens = tokenizer.tokenize("Hello world!");

    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0).getText()).isEqualTo("Hello");
    assertThat(tokens.get(1).getText()).isEqualTo("world");
    assertThat(tokens.get(2).getText()).isEqualTo("!");
    assertThat(tokens.get(0).getStartChar()).isEqualTo(0);
    assertThat(tokens.get(0).getEndChar()).isEqualTo(5);
  }

  @Test
  void testTokenizerUmlauts() {
    // Test tokenization with umlauts
    Tokenizer tokenizer = new Tokenizer();
    List<Token> tokens = tokenizer.tokenize("LG München I");

    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0).getText()).isEqualTo("LG");
    assertThat(tokens.get(1).getText()).isEqualTo("München");
    assertThat(tokens.get(2).getText()).isEqualTo("I");
  }

  @Test
  void testTokenizerWithHyphen() {
    // Test tokenization preserves hyphenated words
    Tokenizer tokenizer = new Tokenizer();
    List<Token> tokens = tokenizer.tokenize("Sachsen-Anhalt");

    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getText()).isEqualTo("Sachsen-Anhalt");
  }

  @Test
  void testTokenizerWithColon() {
    // Test tokenization with colon
    Tokenizer tokenizer = new Tokenizer();
    List<Token> tokens = tokenizer.tokenize("Tenor: Es wird entschieden.");

    assertThat(tokens).hasSize(6);
    assertThat(tokens.get(0).getText()).isEqualTo("Tenor");
    assertThat(tokens.get(1).getText()).isEqualTo(":");
    assertThat(tokens.get(2).getText()).isEqualTo("Es");
    assertThat(tokens.get(3).getText()).isEqualTo("wird");
    assertThat(tokens.get(4).getText()).isEqualTo("entschieden");
    assertThat(tokens.get(5).getText()).isEqualTo(".");
  }

  @Test
  void testPatternMatcherTextExact() {
    // Test exact text matching
    PatternMatcher matcher = new PatternMatcher();
    TokenConstraint constraint =
        new TokenConstraint("hello", null, null, null, null, null, null, null, null, null, null);
    Pattern pattern = Pattern.ofConstraints(List.of(constraint));

    String testStr = "this is hello world";
    List<Match> matches = matcher.match(List.of(pattern), testStr);

    assertThat(matches).hasSize(1);
    String text = testStr.substring(matches.get(0).getStartChar(), matches.get(0).getEndChar());
    assertThat(text).isEqualTo("hello");
  }

  @Test
  void testPatternMatcherLower() {
    // Test lowercase matching
    PatternMatcher matcher = new PatternMatcher();
    TokenConstraint constraint =
        new TokenConstraint(null, "hello", null, null, null, null, null, null, null, null, null);
    Pattern pattern = Pattern.ofConstraints(List.of(constraint));

    List<Match> matches = matcher.match(List.of(pattern), "Hello WORLD");

    assertThat(matches).hasSize(1);
    assertThat(matches.get(0).text()).isEqualTo("Hello");
  }

  @Test
  void testPatternMatcherRegex() {
    // Test regex matching
    PatternMatcher matcher = new PatternMatcher();
    Map<String, Object> regexMap = Map.of("REGEX", "^\\d+$");
    TokenConstraint constraint =
        new TokenConstraint(regexMap, null, null, null, null, null, null, null, null, null, null);
    Pattern pattern = Pattern.ofConstraints(List.of(constraint));

    List<Match> matches = matcher.match(List.of(pattern), "The number is 123 here");

    assertThat(matches).hasSize(1);
    assertThat(matches.get(0).text()).isEqualTo("123");
  }

  @Test
  void testPatternMatcherInList() {
    // Test IN list matching
    PatternMatcher matcher = new PatternMatcher();
    Map<String, Object> inMap = Map.of("IN", List.of("BGH", "BVerfG", "BFH"));
    TokenConstraint constraint =
        new TokenConstraint(inMap, null, null, null, null, null, null, null, null, null, null);
    Pattern pattern = Pattern.ofConstraints(List.of(constraint));

    List<Match> matches = matcher.match(List.of(pattern), "The BGH decided that");
    assertThat(matches).hasSize(1);
    assertThat(matches.get(0).text()).isEqualTo("BGH");

    matches = matcher.match(List.of(pattern), "The BVerfG ruled");
    assertThat(matches).hasSize(1);
    assertThat(matches.get(0).text()).isEqualTo("BVerfG");
  }

  @Test
  void testPatternMatcherIsDigit() {
    // Test IS_DIGIT constraint
    PatternMatcher matcher = new PatternMatcher();
    TokenConstraint constraint =
        new TokenConstraint(null, null, null, null, null, true, null, null, null, null, null);
    Pattern pattern = Pattern.ofConstraints(List.of(constraint));

    List<Match> matches = matcher.match(List.of(pattern), "The year 2024 was good");

    assertThat(matches).hasSize(1);
    assertThat(matches.get(0).text()).isEqualTo("2024");
  }

  @Test
  void testPatternMatcherIsTitle() {
    // Test IS_TITLE constraint
    PatternMatcher matcher = new PatternMatcher();
    Map<String, Object> inMap = Map.of("IN", List.of("LG", "AG"));
    TokenConstraint constraint1 =
        new TokenConstraint(inMap, null, null, null, null, null, null, null, null, null, null);
    TokenConstraint constraint2 =
        new TokenConstraint(null, null, null, null, null, null, null, true, null, null, null);
    Pattern pattern = Pattern.ofConstraints(List.of(constraint1, constraint2));

    List<Match> matches = matcher.match(List.of(pattern), "LG Berlin decided");

    assertThat(matches).hasSize(1);
    assertThat(matches.get(0).text()).isEqualTo("LG Berlin");
  }

  @Test
  void testPatternMatcherShape() {
    // Test SHAPE constraint
    PatternMatcher matcher = new PatternMatcher();
    Map<String, Object> regexMap1 = Map.of("REGEX", "^\\d+\\.?$");
    Map<String, Object> inMap = Map.of("IN", List.of("Januar", "Februar"));

    TokenConstraint constraint1 =
        new TokenConstraint(regexMap1, null, null, null, null, null, null, null, null, null, null);
    TokenConstraint constraint2 =
        new TokenConstraint(inMap, null, null, null, null, null, null, null, null, null, null);
    TokenConstraint constraint3 =
        new TokenConstraint(null, null, null, null, null, null, null, null, null, "dddd", null);

    Pattern pattern = Pattern.ofConstraints(List.of(constraint1, constraint2, constraint3));

    List<Match> matches = matcher.match(List.of(pattern), "Am 15 Januar 2024 wurde entschieden");

    assertThat(matches).hasSize(1);
    assertThat(matches.get(0).text()).isEqualTo("15 Januar 2024");
  }

  @Test
  void testPatternMatcherQuantifierOptional() {
    // Test optional quantifier (?)
    PatternMatcher matcher = new PatternMatcher();
    TokenConstraint constraint1 =
        new TokenConstraint(null, "der", null, null, null, null, null, null, null, null, null);
    TokenConstraint constraint2 =
        new TokenConstraint(null, "große", null, null, null, null, null, null, null, null, "?");
    TokenConstraint constraint3 =
        new TokenConstraint(null, "test", null, null, null, null, null, null, null, null, null);

    Pattern pattern = Pattern.ofConstraints(List.of(constraint1, constraint2, constraint3));

    List<Match> matches = matcher.match(List.of(pattern), "der große test");
    assertThat(matches).hasSize(1);
    assertThat(matches.get(0).text()).isEqualTo("der große test");

    matches = matcher.match(List.of(pattern), "der test");
    assertThat(matches).hasSize(1);
    assertThat(matches.get(0).text()).isEqualTo("der test");
  }

  @Test
  void testPatternMatcherMultiplePatterns() {
    // Test multiple alternative patterns
    PatternMatcher matcher = new PatternMatcher();
    TokenConstraint pattern1Constraint =
        new TokenConstraint(null, "tenor", null, null, null, null, null, null, null, null, null);
    TokenConstraint pattern2Constraint1 =
        new TokenConstraint(
            null, "beschlossen", null, null, null, null, null, null, null, null, null);
    TokenConstraint pattern2Constraint2 =
        new TokenConstraint(":", null, null, null, null, null, null, null, null, null, null);

    Pattern pattern1 = Pattern.ofConstraints(List.of(pattern1Constraint));
    Pattern pattern2 = Pattern.ofConstraints(List.of(pattern2Constraint1, pattern2Constraint2));

    String text = "Tenor: Es wird entschieden. Das Gericht hat beschlossen:";
    List<Match> matches = matcher.match(List.of(pattern1, pattern2), text);

    assertThat(matches).hasSize(2);
  }

  @Test
  void testMatchCharacterPositions() {
    // Test that matches preserve character positions
    PatternMatcher matcher = new PatternMatcher();
    TokenConstraint constraint =
        new TokenConstraint("test", null, null, null, null, null, null, null, null, null, null);
    Pattern pattern = Pattern.ofConstraints(List.of(constraint));

    String text = "This is a test case";
    List<Match> matches = matcher.match(List.of(pattern), text);

    assertThat(matches).hasSize(1);
    assertThat(matches.get(0).getStartChar()).isEqualTo(10);
    assertThat(matches.get(0).getEndChar()).isEqualTo(14);
    assertThat(text.substring(matches.get(0).getStartChar(), matches.get(0).getEndChar()))
        .isEqualTo("test");
  }

  @Test
  void testComplexDatePattern() {
    // Test complex date pattern similar to NER extractor
    PatternMatcher matcher = new PatternMatcher();

    List<String> months =
        List.of(
            "Januar",
            "Februar",
            "März",
            "April",
            "Mai",
            "Juni",
            "Juli",
            "August",
            "September",
            "Oktober",
            "November",
            "Dezember");

    Map<String, Object> regexMap = Map.of("REGEX", "^\\d+\\.?$");
    Map<String, Object> inMap = Map.of("IN", months);

    TokenConstraint constraint1 =
        new TokenConstraint(regexMap, null, null, null, null, null, null, null, null, null, null);
    TokenConstraint constraint2 =
        new TokenConstraint(inMap, null, null, null, null, null, null, null, null, null, null);
    TokenConstraint constraint3 =
        new TokenConstraint(null, null, null, null, null, null, null, null, null, "dddd", null);

    Pattern pattern = Pattern.ofConstraints(List.of(constraint1, constraint2, constraint3));

    String text1 = "Am 24. Dezember 2023 wurde entschieden";
    List<Match> matches = matcher.match(List.of(pattern), text1);
    assertThat(matches).hasSize(1);
    assertThat(matches.get(0).text()).contains("Dezember");
    assertThat(matches.get(0).text()).contains("2023");

    // Also test without period
    String text2 = "Am 24 Dezember 2023 wurde entschieden";
    matches = matcher.match(List.of(pattern), text2);
    assertThat(matches).hasSize(1);
  }

  @Test
  void testTokenizerWithNewlines() {
    // Test tokenizer handles newlines correctly
    Tokenizer tokenizer = new Tokenizer();
    String text = "This is a test.\nNew line here.";
    List<Token> tokens = tokenizer.tokenize(text);

    List<String> expectedTexts =
        List.of("This", "is", "a", "test", ".", "\n", "New", "line", "here", ".");
    assertThat(tokens).hasSize(expectedTexts.size());
    for (int i = 0; i < tokens.size(); i++) {
      assertThat(tokens.get(i).getText()).isEqualTo(expectedTexts.get(i));
    }
  }

  @Test
  void testMatcherWithNewlines() {
    // Test that line breaks in text are handled correctly
    PatternMatcher matcher = new PatternMatcher();
    TokenConstraint constraint1 =
        new TokenConstraint(
            "OBERLANDESGERICHT", null, null, null, null, null, null, null, null, null, null, null);
    TokenConstraint constraint2 =
        new TokenConstraint("\n", null, null, null, null, null, null, null, null, null, null, null);
    TokenConstraint constraint3 =
        new TokenConstraint(null, null, null, null, null, null, null, null, null, null, null, true);

    Pattern pattern = Pattern.ofConstraints(List.of(constraint1, constraint2, constraint3));

    String text = "OBERLANDESGERICHT\nKARLSRUHE";
    List<Match> matches = matcher.match(List.of(pattern), text);
    assertThat(matches).hasSize(1);
    assertThat(matches.get(0).text()).contains("KARLSRUHE");
  }
}
