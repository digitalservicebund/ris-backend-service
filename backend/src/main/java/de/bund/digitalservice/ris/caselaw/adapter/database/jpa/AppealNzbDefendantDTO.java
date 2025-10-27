package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(schema = "incremental_migration", name = "appeal_nzb_defendant")
public class AppealNzbDefendantDTO {

  @EmbeddedId @Builder.Default private AppealNzbDefendantId primaryKey = new AppealNzbDefendantId();

  @ManyToOne
  @MapsId("appealId")
  @JoinColumn(name = "appeal_id")
  private AppealDTO appeal;

  @ManyToOne
  @MapsId("appealStatusId")
  @JoinColumn(name = "appeal_status_id")
  private AppealStatusDTO appealStatus;

  private int rank;
}
