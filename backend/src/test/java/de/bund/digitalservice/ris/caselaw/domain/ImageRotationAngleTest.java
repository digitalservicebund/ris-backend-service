package de.bund.digitalservice.ris.caselaw.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

import de.bund.digitalservice.ris.caselaw.domain.image.ImageRotationAngle;
import de.bund.digitalservice.ris.caselaw.domain.image.ImageRotationAngleException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ImageRotationAngleTest {

  @ParameterizedTest
  @CsvSource({"0", "360"})
  void fromDegrees_shouldThrowException_whenRotationIsZeroOrFullCircle(int rotationAngle) {

    var exception =
        assertThrows(
            ImageRotationAngleException.class, () -> ImageRotationAngle.fromDegrees(rotationAngle));

    Assertions.assertEquals("Rotation is not needed: " + rotationAngle, exception.getMessage());
  }

  @Test
  void fromDegrees_shouldReturnCorrectAngle_whenRotationIsValid() {
    Assertions.assertEquals(ImageRotationAngle.D270, ImageRotationAngle.fromDegrees(-90));
    Assertions.assertEquals(ImageRotationAngle.D90, ImageRotationAngle.fromDegrees(90));
    Assertions.assertEquals(ImageRotationAngle.D180, ImageRotationAngle.fromDegrees(-180));
  }

  @Test
  void fromDegrees_shouldThrowException_whenRotationIsUnsupported() {
    var exception =
        assertThrows(ImageRotationAngleException.class, () -> ImageRotationAngle.fromDegrees(-94));
    Assertions.assertEquals("Unsupported rotation: -94", exception.getMessage());
  }
}
