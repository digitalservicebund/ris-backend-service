package de.bund.digitalservice.ris.caselaw.adapter.publication;

import lombok.Getter;

public enum SyncJob {
  ADM_REVOKED_SYNC("ADM_REVOKED_SYNC"),
  ADM_PASSIVE_CITATION_SYNC("ADM_PASSIVE_CITATION_SYNC"),
  SLI_REVOKED_SYNC("SLI_REVOKED_SYNC"),
  SLI_PASSIVE_CITATION_SYNC("SLI_PASSIVE_CITATION_SYNC"),
  ULI_REVOKED_SYNC("ULI_REVOKED_SYNC"),
  ULI_PASSIVE_CITATION_SYNC("ULI_PASSIVE_CITATION_SYNC");

  @Getter private final String name;

  SyncJob(String name) {
    this.name = name;
  }
}
