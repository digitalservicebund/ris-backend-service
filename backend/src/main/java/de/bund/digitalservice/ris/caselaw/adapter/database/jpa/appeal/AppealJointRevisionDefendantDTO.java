package de.bund.digitalservice.ris.caselaw.adapter.database.jpa.appeal;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(schema = "incremental_migration", name = "appeal_joint_revision_defendant")
public class AppealJointRevisionDefendantDTO {

  @EmbeddedId @Builder.Default
  private AppealJointRevisionDefendantId primaryKey = new AppealJointRevisionDefendantId();

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
    primaryKey = new AppealJointRevisionDefendantId(appealId, appealStatusId);
  }
}

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
class AppealJointRevisionDefendantId implements Serializable {
  private UUID appealId;

  private UUID appealStatusId;

  public AppealJointRevisionDefendantId(UUID appealId, UUID appealStatusId) {
    this.appealId = appealId;
    this.appealStatusId = appealStatusId;
  }
}
