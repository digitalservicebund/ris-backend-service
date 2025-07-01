package de.bund.digitalservice.ris.caselaw.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

  @Test
  void testReturnTrueIfNullOrBlank() {
    // Test with null input
    assertTrue(StringUtils.returnTrueIfNullOrBlank(null));

    // Test with empty string
    assertTrue(StringUtils.returnTrueIfNullOrBlank(""));

    // Test with blank string (spaces)
    assertTrue(StringUtils.returnTrueIfNullOrBlank("   "));

    // Test with blank string (tabs)
    assertTrue(StringUtils.returnTrueIfNullOrBlank("\t"));

    // Test with blank string (newlines)
    assertTrue(StringUtils.returnTrueIfNullOrBlank("\n"));

    // Test with non-blank string
    assertFalse(StringUtils.returnTrueIfNullOrBlank("abc"));

    // Test with string containing spaces
    assertFalse(StringUtils.returnTrueIfNullOrBlank(" abc "));
  }

  @Test
  void testNormalizeSpace() {
    // Test with null input
    assertNull(StringUtils.normalizeSpace(null));

    // Test with empty string
    assertEquals("", StringUtils.normalizeSpace(""));

    // Test with normal spaces
    assertEquals("a b c", StringUtils.normalizeSpace("  a  b  c  "));

    // Test with leading and trailing spaces
    assertEquals("a b c", StringUtils.normalizeSpace("   a b c   "));

    // Test with various Unicode spaces
    assertEquals("a b c", StringUtils.normalizeSpace("a\u00A0b\u202Fc\uFEFF"));
    assertEquals("a b c", StringUtils.normalizeSpace("a\u2007b\u180Ec\u2060"));

    // Test with non-breaking spaces
    assertEquals(
        "This is a test string with spaces",
        StringUtils.normalizeSpace(
            "This\u00A0is\u202Fa\uFEFFtest\u2007string\u180Ewith\u2060spaces"));

    // Test with mixed spaces
    assertEquals("a b c", StringUtils.normalizeSpace("  a \u00A0 b \t c  "));

    // Test with non-space Unicode characters
    assertEquals("a b c", StringUtils.normalizeSpace("a \u2009 b \u3000 c"));
  }
}
