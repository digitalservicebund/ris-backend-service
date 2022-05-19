package de.bund.digitalservice.ris.datamodel;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VersionInfo {

  private String version;
  private String commitSHA;

  @Builder.Default
  private String repository = "https://github.com/digitalservice4germany/ris-backend-service";
}
