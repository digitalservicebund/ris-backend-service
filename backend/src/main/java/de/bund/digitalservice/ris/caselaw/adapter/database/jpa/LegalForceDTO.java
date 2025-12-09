package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
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
@Table(name = "legal_force", schema = "incremental_migration")
public class LegalForceDTO {
  @Id @GeneratedValue private UUID id;

  @ManyToOne
  @JoinColumn(name = "legal_force_type_id")
  private LegalForceTypeDTO legalForceType;

  @ManyToOne
  @JoinColumn(name = "region_id")
  private RegionDTO region;

  @OneToOne()
  @JoinColumn(name = "norm_reference_id", referencedColumnName = "id")
  private AbstractNormReferenceDTO normReference;
}
