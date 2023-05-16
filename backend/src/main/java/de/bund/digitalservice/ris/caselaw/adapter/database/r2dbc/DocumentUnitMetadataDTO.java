package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("doc_unit")
public class DocumentUnitMetadataDTO {

  @Id private Long id;
  UUID uuid;
  private String documentnumber;
  private Instant creationtimestamp;
  private DataSource dataSource;

  // Original file
  private Instant fileuploadtimestamp;
  private String s3path;
  private String filetype;
  private String filename;

  // - Stammdaten
  @Column("gerichtstyp")
  private String courtType;

  @Column("document_type_id")
  private Long documentTypeId;

  @Transient private DocumentTypeDTO documentTypeDTO;

  @Column("vorgang")
  private String procedure;

  @Column("ecli")
  private String ecli;

  @Column("spruchkoerper")
  private String appraisalBody;

  @Column("decision_date")
  private Instant decisionDate;

  @Column("date_known")
  private boolean dateKnown;

  @Column("gerichtssitz")
  private String courtLocation;

  @Column("rechtskraft")
  private String legalEffect;

  @Column("eingangsart")
  private String inputType;

  @Column("dokumentationsstelle")
  private String center;

  @Column("region")
  private String region;

  @Transient private List<FileNumberDTO> fileNumbers;
}
