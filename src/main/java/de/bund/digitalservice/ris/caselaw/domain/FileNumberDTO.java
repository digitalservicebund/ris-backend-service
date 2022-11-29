package de.bund.digitalservice.ris.caselaw.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("file_number")
public class FileNumberDTO {
  @Id Long id;
  Long documentUnitId;
  String fileNumber;
  Boolean isDeviating;
}
