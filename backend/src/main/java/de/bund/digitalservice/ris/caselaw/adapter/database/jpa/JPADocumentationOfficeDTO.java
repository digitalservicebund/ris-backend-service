package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "documentation_office")
public class JPADocumentationOfficeDTO {
  @Id private UUID id;

  @Column(name = "label")
  private String label;

  @Column(name = "abbreviation")
  private String abbreviation;
}
