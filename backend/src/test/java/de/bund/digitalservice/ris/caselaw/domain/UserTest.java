package de.bund.digitalservice.ris.caselaw.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UserTest {

  private static Stream<Arguments> nameAndInitialsTestCases() {
    return Stream.of(
        Arguments.of("John", "Doe", "John Doe", "JD"),
        Arguments.of("Jane", null, "Jane", "J"),
        Arguments.of(null, "Smith", "Smith", "S"),
        Arguments.of(null, null, null, null),
        Arguments.of("  ", "  ", null, null),
        Arguments.of("  ", "Doe", "Doe", "D"),
        Arguments.of("John", "  ", "John", "J"));
  }

  @ParameterizedTest
  @MethodSource("nameAndInitialsTestCases")
  void withVariousNames_returnsCorrectNameAndInitials(
      String firstName, String lastName, String expectedName, String expectedInitials) {
    User user = User.builder().firstName(firstName).lastName(lastName).build();

    String actualName = user.name();
    String actualInitials = user.initials();

    assertEquals(expectedName, actualName);
    assertEquals(expectedInitials, actualInitials);
  }

  @Test
  void toString_withUserId_returnsFormattedString() {
    UUID userId = UUID.randomUUID();
    User user = User.builder().id(userId).build();

    String userString = user.toString();

    assertEquals("User[id=%s]".formatted(userId), userString);
  }
}
