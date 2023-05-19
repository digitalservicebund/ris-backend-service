package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("document_type")
public class DocumentTypeNewDTO {
  @Id UUID id;
  String abbreviation;
  String label;
  boolean multiple;
  String superLabel1;
  String superLabel2;
  UUID documentCategoryId;
  @Transient Character categoryLabel;
}
