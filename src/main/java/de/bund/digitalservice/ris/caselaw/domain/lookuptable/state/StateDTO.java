package de.bund.digitalservice.ris.caselaw.domain.lookuptable.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("lookuptable_state")
public class StateDTO {
  public static final StateDTO EMPTY = new StateDTO();
  long id;
  char changeindicator;
  String version;
  String jurisshortcut;
  String label;
}
