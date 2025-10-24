package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

/** DE: Rechtsmittelzulassung */
@Builder
public record AppealAdmission(boolean admitted, AppealAdmitter by) {}
