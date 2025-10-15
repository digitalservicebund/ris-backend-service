package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

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
@Builder
@Entity
@Table(schema = "incremental_migration", name = "court_region")
public class CourtRegionDTO {

  @EmbeddedId @Builder.Default private CourtRegionId primaryKey = new CourtRegionId();

  @ManyToOne
  @MapsId("courtId")
  @JoinColumn(name = "court_id")
  private CourtDTO court;

  @ManyToOne
  @MapsId("regionId")
  @JoinColumn(name = "region_id")
  private RegionDTO region;

  private int rank;
}

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
class CourtRegionId implements Serializable {
  private UUID courtId;

  private UUID regionId;

  public CourtRegionId(UUID courtId, UUID regionId) {
    this.courtId = courtId;
    this.regionId = regionId;
  }
}
