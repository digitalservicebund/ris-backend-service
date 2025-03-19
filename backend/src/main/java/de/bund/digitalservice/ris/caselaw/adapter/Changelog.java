package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Changelog(List<String> changed, List<String> deleted) {
  public String createFileName() {
    return "changelogs/" + DateUtils.toDateTimeString(LocalDateTime.now()) + "-caselaw.json";
  }
}
