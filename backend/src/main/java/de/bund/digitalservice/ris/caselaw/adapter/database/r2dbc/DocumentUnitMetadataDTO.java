package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import java.time.Instant;
import java.time.LocalDate;
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

/**
 * @deprecated use {@link
 *     de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitMetadataDTO} instead
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("doc_unit")
@Deprecated
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
  private UUID documentTypeId;

  @Transient private DocumentTypeDTO documentTypeDTO;

  @Column("ecli")
  private String ecli;

  @Column("spruchkoerper")
  private String appraisalBody;

  @Column("decision_date")
  private LocalDate decisionDate;

  @Column("date_known")
  private boolean dateKnown;

  @Column("gerichtssitz")
  private String courtLocation;

  @Column("rechtskraft")
  private String legalEffect;

  @Column("eingangsart")
  private String inputType;

  @Column("region")
  private String region;

  @Column("documentation_office_id")
  private UUID documentationOfficeId;

  @Transient private DocumentationOfficeDTO documentationOffice;

  @Transient private List<FileNumberDTO> fileNumbers;

  @Transient private DocumentUnitStatus status;

  @Transient private ProcedureDTO procedure;

  @Transient private List<String> previousProcedures;
}
