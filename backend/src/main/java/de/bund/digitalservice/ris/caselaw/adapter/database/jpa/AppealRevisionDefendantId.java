package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class AppealRevisionDefendantId implements Serializable {
  private UUID appealId;

  private UUID appealStatusId;

  public AppealRevisionDefendantId(UUID appealId, UUID appealStatusId) {
    this.appealId = appealId;
    this.appealStatusId = appealStatusId;
  }
}
