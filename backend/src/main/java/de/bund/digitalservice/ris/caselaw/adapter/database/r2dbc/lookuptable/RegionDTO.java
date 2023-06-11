package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("region")
public class RegionDTO implements Persistable<UUID> {
  @Id private UUID id;
  private String code;
  private String label;
  @Transient private boolean newEntity;

  @Override
  public boolean isNew() {
    return id == null || newEntity;
  }
}
