package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import java.time.Year;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_number")
public class DocumentNumberDTO {

  @Id @GeneratedValue private UUID id;

  @Column(name = "documentation_office_abbreviation")
  @NotEmpty
  private String documentationOfficeAbbreviation;

  @Column(name = "last_number")
  private int lastNumber;

  @Column(name = "year")
  @NotNull
  private Year year;

  public Integer increaseLastNumber() {
    this.lastNumber = lastNumber + 1;
    return lastNumber;
  }
}
