package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Changelog(List<String> changed, List<String> deleted) {
  public String createFileName() {
    return "changelogs/" + Instant.now().toString() + "-caselaw.json";
  }
}
