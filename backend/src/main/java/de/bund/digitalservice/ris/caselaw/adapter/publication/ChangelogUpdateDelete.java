package de.bund.digitalservice.ris.caselaw.adapter.publication;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChangelogUpdateDelete(List<String> changed, List<String> deleted)
    implements Changelog {}
