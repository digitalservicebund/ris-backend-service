package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("doc_unit")
public class DocumentUnitListEntry {
  @Id Long id;
  UUID uuid;

  @Column("documentnumber")
  String documentNumber;

  @Column("creationtimestamp")
  Instant creationTimestamp;

  String dataSource;

  @Column("filename")
  String fileName;

  @Transient String fileNumber;
}
