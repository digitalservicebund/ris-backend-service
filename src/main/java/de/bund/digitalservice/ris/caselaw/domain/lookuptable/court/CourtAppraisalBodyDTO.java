package de.bund.digitalservice.ris.caselaw.domain.lookuptable.court;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtAppraisalBodyDTO {
  String name;
  CourtNumberSpecificationDTO numberspecification;
}
