package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType.Values;
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
@DiscriminatorValue(Values.ACTIVE_CITATION)
public class ActiveCitationDTO extends RelatedDocumentationDTO {

  @ManyToOne
  @JoinColumn(name = "citation_type_id")
  private CitationTypeDTO citationType;

  @Override
  public boolean equals(Object other) {
    return super.equals(other);
  }
}
