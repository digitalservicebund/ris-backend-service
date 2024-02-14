package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_number", schema = "public")
public class DocumentNumberDTO {

  @Id
  @Column(name = "documentationOfficeAbbreviation")
  @NotEmpty
  private String documentationOfficeAbbreviation;

  @Column(name = "lastNumber")
  private int lastNumber;

  public Integer increaseLastNumber() {
    this.lastNumber = lastNumber + 1;
    return lastNumber;
  }
}
