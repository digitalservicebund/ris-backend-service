package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("lookuptable_field_of_law_link")
public class FieldOfLawLinkDTO implements Persistable<Long> {
  public static final FieldOfLawLinkDTO EMPTY = new FieldOfLawLinkDTO();
  @Id Long id;
  Long fieldOfLawId;
  Long linkedFieldOfLawId;

  @Transient private boolean isNew;

  @Override
  public boolean isNew() {
    return this.isNew || id == null;
  }
}
