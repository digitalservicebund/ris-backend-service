package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignDocumentationOfficeRequest {
  @NotNull private DocumentationOffice documentationOffice;
}
