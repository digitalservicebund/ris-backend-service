package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Getter
@Entity
@DiscriminatorValue("caselaw_active_citation")
public class ActiveCitationDTO extends RelatedDocumentationDTO {

  @ManyToOne
  @JoinColumn(name = "citation_type_id")
  private CitationTypeDTO citationType;
}
