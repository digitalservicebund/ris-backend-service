package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documentation_office_user_group", schema = "incremental_migration")
public class DocumentationOfficeUserGroupDTO {
  @Id @GeneratedValue private UUID id;

  /** Represents a Bare.ID group, e.g. "/caselaw/BGH/Internal" */
  @Column(name = "user_group_path_name")
  @NotNull
  private String userGroupPathName;

  @Column(name = "is_internal")
  @NotNull
  private boolean isInternal;

  @ManyToOne(optional = false)
  @NotNull
  @JoinColumn(name = "documentation_office_id", referencedColumnName = "id")
  private DocumentationOfficeDTO documentationOffice;
}
