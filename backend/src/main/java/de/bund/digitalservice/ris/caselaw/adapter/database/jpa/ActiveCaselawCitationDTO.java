package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

public sealed interface ActiveCaselawCitationDTO
    permits ActiveBlindlinkCaselawCitationDTO, LinkCaselawCitationDTO {
  Integer getRank();
}
