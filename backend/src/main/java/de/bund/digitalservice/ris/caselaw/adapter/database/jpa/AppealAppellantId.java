package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class AppealAppellantId implements Serializable {
  private UUID appealId;

  private UUID appellantId;

  public AppealAppellantId(UUID appealId, UUID appellantId) {
    this.appealId = appealId;
    this.appellantId = appellantId;
  }
}
