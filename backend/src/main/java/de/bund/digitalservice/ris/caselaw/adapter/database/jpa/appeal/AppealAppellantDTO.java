package de.bund.digitalservice.ris.caselaw.adapter.database.jpa.appeal;

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
@Table(schema = "incremental_migration", name = "appeal_appellant")
public class AppealAppellantDTO {

  @EmbeddedId @Builder.Default private AppealAppellantId primaryKey = new AppealAppellantId();

  @ManyToOne
  @MapsId("appealId")
  @JoinColumn(name = "appeal_id")
  private AppealDTO appeal;

  @ManyToOne
  @MapsId("appellantId")
  @JoinColumn(name = "appellant_id")
  private AppellantDTO appellant;

  private int rank;
}
