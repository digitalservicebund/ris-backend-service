package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(
    schema = "incremental_migration",
    name = "user",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"first_name", "last_name", "documentation_office_id"}))
public class UserDTO {

  @Id @GeneratedValue private UUID id;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @ManyToOne
  @JoinColumn(name = "documentation_office_id")
  private DocumentationOfficeDTO documentationOffice;

  @Column(name = "is_active")
  private boolean isActive;

  @Column(name = "is_deleted")
  private boolean isDeleted;
}
