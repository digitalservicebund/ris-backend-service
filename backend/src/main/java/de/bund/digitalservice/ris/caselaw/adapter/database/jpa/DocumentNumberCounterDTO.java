package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_number_counter")
public class DocumentNumberCounterDTO {
  @Id private Long id;

  @Column(name = "nextnumber")
  private Integer nextNumber;

  @Column(name = "currentyear")
  private Integer currentYear;
}
