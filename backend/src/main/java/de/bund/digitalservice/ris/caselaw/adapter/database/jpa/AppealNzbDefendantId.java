package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class AppealNzbDefendantId implements Serializable {
  private UUID appealId;

  private UUID appealStatusId;

  public AppealNzbDefendantId(UUID appealId, UUID appealStatusId) {
    this.appealId = appealId;
    this.appealStatusId = appealStatusId;
  }
}
