package de.bund.digitalservice.ris.datamodel;

import lombok.Data;

@Data
public class VersionInfo {

  private String version;
  private String commitSHA;
  private String repository = "https://github.com/digitalservice4germany/ris-backend-service";
}
