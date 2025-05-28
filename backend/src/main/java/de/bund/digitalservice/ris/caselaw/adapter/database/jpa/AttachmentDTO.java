package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "attachment", schema = "incremental_migration")
public class AttachmentDTO {

  @Id @GeneratedValue @NotNull private UUID id;

  @Column(name = "upload_timestamp")
  private Instant uploadTimestamp;

  @Column private String format;

  @Column private String filename;

  @Column(name = "s3_object_path")
  private String s3ObjectPath;

  @ManyToOne
  @JoinColumn(name = "documentation_unit_id")
  private DocumentationUnitDTO documentationUnit;
}
