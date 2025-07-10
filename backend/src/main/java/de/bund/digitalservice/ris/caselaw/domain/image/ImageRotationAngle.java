package de.bund.digitalservice.ris.caselaw.domain.image;

public enum ImageRotationAngle {
  D90(90),
  D180(180),
  D270(270);

  private final int degrees;

  ImageRotationAngle(int degrees) {
    this.degrees = degrees;
  }

  public double getRadians() {
    return Math.toRadians(degrees);
  }

  public static ImageRotationAngle fromDegrees(int degrees) {
    // in case rotation is negative
    int normalizedAngle = ((degrees % 360) + 360) % 360;
    if (normalizedAngle == 90) {
      return D90;
    } else if (normalizedAngle == 180) {
      return D180;
    } else if (normalizedAngle == 270) {
      return D270;
    } else if (normalizedAngle == 0) {
      throw new ImageRotationAngleException("Rotation is not needed: " + degrees);
    } else {
      throw new ImageRotationAngleException("Unsupported rotation: " + degrees);
    }
  }
}
