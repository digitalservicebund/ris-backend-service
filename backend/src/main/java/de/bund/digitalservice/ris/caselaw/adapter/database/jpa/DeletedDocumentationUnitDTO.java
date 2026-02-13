package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import java.time.Year;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "deleted_documents")
public class DeletedDocumentationUnitDTO {

  @Id
  @Column(name = "document_number")
  @NotEmpty
  private String documentNumber;

  @Column(name = "year")
  @NotNull
  private Year year;

  @Column(name = "abbreviation")
  @NotEmpty
  private String abbreviation;
}
