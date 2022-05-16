package de.bund.digitalservice.ris.datamodel;

import lombok.Data;

@Data
public class VersionInfo {

  private String version;
  private String commitSHA;
}
