package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(schema = "incremental_migration", name = "appeal_nzb_plaintiff")
public class AppealNzbPlaintiffDTO {

  @EmbeddedId @Builder.Default private AppealNzbPlaintiffId primaryKey = new AppealNzbPlaintiffId();

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
