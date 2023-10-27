package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "court", schema = "incremental_migration")
public class CourtDTO {
  @Id @GeneratedValue private UUID id;

  @Column @NotBlank private String type;

  @Column private String location;

  @Column(name = "is_superior_court")
  @NotNull
  @Accessors(fluent = true)
  private Boolean isSuperiorCourt;

  @Column(name = "is_foreign_court")
  @NotNull
  @Accessors(fluent = true)
  private Boolean isForeignCourt;

  @Column(name = "additional_information")
  private String additionalInformation;
}
