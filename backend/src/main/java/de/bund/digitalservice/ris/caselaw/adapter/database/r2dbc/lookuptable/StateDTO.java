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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("lookuptable_state")
public class StateDTO implements Persistable<Long> {
  public static final StateDTO EMPTY = new StateDTO();
  @Id Long id;
  Character changeindicator;
  String version;
  String jurisshortcut;
  String label;

  @Transient private boolean newEntry;

  @Override
  @Transient
  public boolean isNew() {
    return this.newEntry || id == null;
  }
}
