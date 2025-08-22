package de.bund.digitalservice.ris.caselaw.domain;

public enum HistoryLogEventType {
  CREATE,
  UPDATE,
  FILES,
  PROCEDURE,
  STATUS,
  DOCUMENTATION_OFFICE,
  HANDOVER,
  EXTERNAL_HANDOVER,
  LEGACY_DOKEDIT,
  LEGACY_DOKABGABE,
  SCHEDULED_PUBLICATION,
  RESOLVE_PENDING_PROCEEDING,
  PROCESS_STEP,
  PROCESS_STEP_USER,
  PORTAL_PUBLICATION
  // ⚠ CAUTION ⚠: Please add any new Enum in the HistoryLogEventType in the
  // ris-data-migration repository to avoid errors during migration!
}
