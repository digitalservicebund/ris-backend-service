package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
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
@Table(name = "original_xml")
public class OriginalXmlDTO {

  @Id
  @Column(name = "documentation_unit_id", updatable = false, nullable = false)
  private UUID documentationUnitId;

  @Column(name = "created_at")
  @NotNull
  private Instant createdAt;

  @Column(name = "updated_at")
  @NotNull
  private Instant updatedAt;

  @Column
  @Enumerated(EnumType.STRING)
  Type type;

  @Column @NotBlank private String content;

  @Column private String source;

  public enum Type {
    JURIS,
    FMX,
  }
}
