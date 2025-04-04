package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Changelog(
    List<String> changed, List<String> deleted, @JsonProperty("change_all") Boolean changeAll) {
  public String createFileName() {
    return "changelogs/" + Instant.now().toString() + "-caselaw.json";
  }
}
