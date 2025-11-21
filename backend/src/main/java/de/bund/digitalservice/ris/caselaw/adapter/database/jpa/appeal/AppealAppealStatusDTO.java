package de.bund.digitalservice.ris.caselaw.adapter.database.jpa.appeal;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.MapsId;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(builderMethodName = "appealAppealStatusBuilder")
@MappedSuperclass
public class AppealAppealStatusDTO {

  @EmbeddedId @Builder.Default private AppealAppealStatusId primaryKey = new AppealAppealStatusId();

  @ManyToOne
  @MapsId("appealId")
  @JoinColumn(name = "appeal_id")
  private AppealDTO appeal;

  @ManyToOne
  @MapsId("appealStatusId")
  @JoinColumn(name = "appeal_status_id")
  private AppealStatusDTO appealStatus;

  private int rank;

  public void setPrimaryKey(UUID appealId, UUID appealStatusId) {
    primaryKey = new AppealAppealStatusId(appealId, appealStatusId);
  }
}

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
class AppealAppealStatusId implements Serializable {
  private UUID appealId;

  private UUID appealStatusId;

  public AppealAppealStatusId(UUID appealId, UUID appealStatusId) {
    this.appealId = appealId;
    this.appealStatusId = appealStatusId;
  }
}
