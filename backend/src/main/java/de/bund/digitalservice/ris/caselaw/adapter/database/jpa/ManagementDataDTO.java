package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "management_data")
public class ManagementDataDTO {

  @Id
  @Column(name = "documentation_unit_id")
  private UUID id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "documentation_unit_id")
  private DocumentationUnitDTO documentationUnit;

  @OneToOne()
  @JoinColumn(name = "created_by_documentation_office", referencedColumnName = "id")
  private DocumentationOfficeDTO createdByDocumentationOffice;

  @Column(name = "created_by_user_id")
  private UUID createdByUserId;

  @Column(name = "created_at_date_time")
  private Instant createdAtDateTime;

  @Column(name = "created_by_user_name")
  private String createdByUserName;

  @Column(name = "created_by_system_name")
  private String createdBySystemName;

  @OneToOne()
  @JoinColumn(name = "last_updated_by_documentation_office", referencedColumnName = "id")
  private DocumentationOfficeDTO lastUpdatedByDocumentationOffice;

  @Column(name = "last_updated_by_user_id")
  private UUID lastUpdatedByUserId;

  @Column(name = "last_updated_at_date_time")
  private Instant lastUpdatedAtDateTime;

  @Column(name = "last_updated_by_user_name")
  private String lastUpdatedByUserName;

  @Column(name = "last_updated_by_system_name")
  private String lastUpdatedBySystemName;

  @Column(name = "first_published_at_date_time")
  private Instant firstPublishedAtDateTime;

  @Column(name = "last_published_at_date_time")
  private Instant lastPublishedAtDateTime;
}
