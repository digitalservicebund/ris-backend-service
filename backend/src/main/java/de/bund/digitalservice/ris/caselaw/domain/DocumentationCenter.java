package de.bund.digitalservice.ris.caselaw.domain;

public enum DocumentationCenter {
  BGH,
  BVerfG,
  DigitalService,
  CCRIS {
    @Override
    public String toString() {
      return "CC-RIS";
    }
  }
}
