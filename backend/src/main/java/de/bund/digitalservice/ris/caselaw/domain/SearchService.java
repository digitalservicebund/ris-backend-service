package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.data.domain.Page;

public interface SearchService {
  Page<SearchResult> getSearchResults(String page);
}
