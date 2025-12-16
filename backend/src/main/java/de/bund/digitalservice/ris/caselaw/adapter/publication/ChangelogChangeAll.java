package de.bund.digitalservice.ris.caselaw.adapter.publication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ChangelogChangeAll(@JsonProperty("change_all") boolean changeAll)
    implements Changelog {}
