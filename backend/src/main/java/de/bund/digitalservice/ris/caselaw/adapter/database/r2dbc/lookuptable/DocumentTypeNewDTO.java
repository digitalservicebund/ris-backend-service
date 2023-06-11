package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("document_type")
public class DocumentTypeNewDTO implements Persistable<UUID> {
  @Id private UUID id;
  private String abbreviation;
  private String label;
  private boolean multiple;

  @Column("super_label_1")
  private String superLabel1;

  @Column("super_label_2")
  private String superLabel2;

  private UUID documentCategoryId;
  @Transient private Character categoryLabel;
  @Transient private boolean newEntity = false;

  @Override
  public boolean isNew() {
    return id == null || newEntity;
  }
}
